package com.boctool.webservice_engine.service;

import com.boctool.webservice_engine.controller.RequestController;
import com.boctool.webservice_engine.entity.Query;
import com.boctool.webservice_engine.entity.QueryDTO;
import com.boctool.webservice_engine.repository.QueryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.boctool.webservice_engine.utils.Utilities.*;

@Service
public class QueryService {

    @Autowired
    private final QueryRepository queryRepository;

    public QueryService(QueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }
    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);

    public ResponseEntity<Object> saveQueries(List<QueryDTO> queryDTOS){
        Map<String, Object> log = new HashMap<>();
        int i = 1;

        for (QueryDTO queryDTO : queryDTOS) {
            String sql = queryDTO.getSql();
            Map<String, String> parameters = queryDTO.getParameters();
            String queryText = normalizeSql(sql);
            String queryMd5 = convertQueryToMD5(queryText);

            try {
                if (existsByQueryMd5(queryMd5)) {
                    log.put(""+i++, "Error: sql statement already exists " + queryMd5);
                    continue;
                }

                validateQuery(sql, parameters);

                validateInputTypeParameters(parameters);

                Query query = new Query();
                query.setQueryMd5(queryMd5);
                query.setQueryText(queryText);
                query.setQueryParams(new ObjectMapper().writeValueAsString(parameters));
                query.setQueryRegdate(new Date());
                queryRepository.save(query);

                log.put(""+i++, query);

            } catch (JsonProcessingException e) {
                log.put(""+i++, "Error: processing parameters for query " + queryMd5 + ": " + e.getMessage());
            } catch (Exception e) {
                log.put(""+i++, "Error: saving query " + queryMd5 + ": " + e.getMessage());
            }
        }

        return new ResponseEntity<>(log, HttpStatus.OK);
    }


    public boolean existsByQueryMd5(String queryMd5) {
        return queryRepository.findQueryByQueryMd5(queryMd5) != null;
    }

    public List<Query> findAllQueries() {
        return queryRepository.findAll();
    }

    public void deleteAllQueries() {
        queryRepository.deleteAll();
    }


    private void validateQuery(String sql, Map<String, String> parameters) {
        Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
        Matcher matcher = pattern.matcher(sql);
        Set<String> sqlParams = new HashSet<>();

        while (matcher.find()) {
            sqlParams.add(matcher.group(1));
        }

        for (String param : sqlParams) {
            if (!parameters.containsKey(param)) {
                throw new IllegalArgumentException("El parámetro '" + param + "' en el SQL no está presente en los parametros.");
            }
        }

        for (String paramKey : parameters.keySet()) {
            if (!sqlParams.contains(paramKey)) {
                throw new IllegalArgumentException("El parámetro '" + paramKey + "' en los parametros no aparece en el SQL.");
            }
        }

        logger.info("Parameters validated successfully");
    }
}
