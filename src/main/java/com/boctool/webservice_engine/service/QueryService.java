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

import static com.boctool.webservice_engine.utils.Utils.convertQueryToMD5;
import static com.boctool.webservice_engine.utils.Utils.normalizeSql;

@Service
public class QueryService {

    @Autowired
    private QueryRepository queryRepository;


    public ResponseEntity<Object> saveQueries(List<QueryDTO> queryDTOS){
        Map<String, Object> response = new HashMap<>();
        int i = 1;

        for (QueryDTO queryDTO : queryDTOS) {
            String sql = queryDTO.getSql();
            Map<String, String> parameters = queryDTO.getParameters();
            String queryText = normalizeSql(sql);
            String queryMd5 = convertQueryToMD5(queryText);

            try {
                if (existsByQueryMd5(queryMd5)) {
                    response.put(""+i++, "Error: sql statement already exists " + queryMd5);
                    continue;
                }

                Query query = new Query();
                query.setQueryMd5(queryMd5);
                query.setQueryText(queryText);
                query.setQueryParams(new ObjectMapper().writeValueAsString(parameters));
                query.setQueryRegdate(new Date());
                queryRepository.save(query);

                response.put(""+i++, query);

            } catch (JsonProcessingException e) {
                response.put(""+i++, "Error: processing parameters for query " + queryMd5 + ": " + e.getMessage());
            } catch (Exception e) {
                response.put(""+i++, "Error: saving query " + queryMd5 + ": " + e.getMessage());
            }
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    public boolean existsByQueryMd5(String queryMd5) {
        if (queryRepository.findQueryByQueryMd5(queryMd5) == null){
            return false;
        } else{
            return true;
        }
    }

    public List<Query> findAllQueries() {
        return queryRepository.findAll();
    }

    public void deleteAllQueries() {
        queryRepository.deleteAll();
    }
}
