package com.boctool.webservice_engine.service;

import com.boctool.webservice_engine.entity.Query;
import com.boctool.webservice_engine.entity.QueryDTO;
import com.boctool.webservice_engine.repository.QueryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.boctool.webservice_engine.utils.Utilities.*;

@Service
public class QueryService {

    @Autowired
    private final QueryRepository queryRepository;

    public QueryService(QueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    private static final Set<String> ALLOWED_TYPES = Set.of("char", "integer", "date", "datetime");


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
}
