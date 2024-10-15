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

    private final QueryRepository queryRepository;

    @Autowired
    public QueryService(QueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    private static final Set<String> ALLOWED_TYPES = Set.of("char", "integer", "date", "datetime", "array_char", "array_integer", "function");
    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);

    public ResponseEntity<Object> saveQueries(List<QueryDTO> queryDTOS) {
        Map<String, Object> log = new HashMap<>();
        int i = 1;

        for (QueryDTO queryDTO : queryDTOS) {
            String sql = queryDTO.getSql();
            Map<String, String> parameters = queryDTO.getParameters();
            String normalizedQueryText = normalizeSql(sql);
            String queryMd5 = convertTextToMd5(normalizedQueryText);

            try {
                if (existsByQueryMd5(queryMd5)) {
                    log.put("" + i++, "Error sql statement already exists " + queryMd5);
                    continue;
                }

                validateQuery(sql, parameters);

                validateInputTypeParameters(parameters);

                Query query = new Query();
                query.setQueryMd5(queryMd5);
                query.setQueryText(sql);
                query.setQueryParams(new ObjectMapper().writeValueAsString(parameters));
                query.setQueryRegdate(new Date());
                queryRepository.save(query);

                log.put("" + i++, query);

            } catch (JsonProcessingException e) {
                log.put("" + i++, "Error processing parameters for query " + queryMd5 + ": " + e.getMessage());
            } catch (Exception e) {
                log.put("" + i++, "Error saving query " + queryMd5 + ": " + e.getMessage());
            }
        }

        return new ResponseEntity<>(log, HttpStatus.OK);
    }

    public static String normalizeSql(String sql) {

        sql = sql.replace(";", "");

        String[] parts = sql.split(REGEX_BRACE_PATTER);

        StringBuilder cleanSql = new StringBuilder();

        Pattern pattern = Pattern.compile(REGEX_BRACE_PATTER);
        Matcher matcher = pattern.matcher(sql);

        int i = 0;

        while (matcher.find()) {
            cleanSql.append(parts[i++].toLowerCase());
            cleanSql.append(matcher.group());
        }

        if (i < parts.length) {
            cleanSql.append(parts[i].toLowerCase());
        }

        return cleanSql.toString();
    }

    public static void validateInputTypeParameters(Map<String, String> parameters) {
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String paramType = entry.getValue().toLowerCase();
            if (!ALLOWED_TYPES.contains(paramType)) {
                throw new IllegalArgumentException("Invalid parameter type: " + paramType + " for key: " + entry.getKey());
            }
        }
    }

    private void validateQuery(String sql, Map<String, String> parameters) {
        Pattern pattern = Pattern.compile(REGEX_BRACE_PATTER);
        Matcher matcher = pattern.matcher(sql);
        Set<String> sqlParams = new HashSet<>();

        while (matcher.find()) {
            sqlParams.add(matcher.group(1));
        }
        for (String param : sqlParams) {
            if (!parameters.containsKey(param)) {
                throw new IllegalArgumentException("Error, the parameter '" + param + "' in the SQL is not defined in the parameters key.");
            }
        }
        for (String paramKey : parameters.keySet()) {
            if (!sqlParams.contains(paramKey)) {
                throw new IllegalArgumentException("Error, the parameter '" + paramKey + "' is not defined in the SQL statement");
            }
        }

        logger.info("Parameters validated successfully");
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

    public Query findQueryById(String id) {
        return queryRepository.findQueryByQueryId(id);
    }
}
