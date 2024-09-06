package com.boctool.webservice_engine.service;

import com.boctool.webservice_engine.controller.ResponseController;
import com.boctool.webservice_engine.entity.*;
import com.boctool.webservice_engine.repository.QueryRepository;
import com.boctool.webservice_engine.repository.RequestRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.boctool.webservice_engine.utils.Utils.*;

@Service
public class RequestService {

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    QueryRepository queryRepository;

    @Autowired
    SourceService sourceService;

    private static final Logger logger = LoggerFactory.getLogger(ResponseController.class);


    public RequestService(RequestRepository requestRepository, QueryRepository queryRepository, SourceService sourceService) {
        this.requestRepository = requestRepository;
        this.queryRepository = queryRepository;
        this.sourceService = sourceService;
    }

    public List<Request> findAllRequests() {
        return requestRepository.findAll();
    }

    public ResponseEntity<Object> executeQueries(List<RequestDTO> requestDTOS) {
        Map<String, Object> response = new HashMap<>();
        int i = 1;

        for (RequestDTO requestDTO : requestDTOS) {
            String sqlMd5 = requestDTO.getSql();
            String sourceId = requestDTO.getSource();
            Map<String, Object> parameterValues = requestDTO.getParameters();

            Query query = queryRepository.findQueryByQueryMd5(sqlMd5);
            if (query == null) {
                response.put(""+i++, "Error: sql statement does not exists in the database: " + sqlMd5);
                continue;
            }

            logger.info("Query: {}", query);
            boolean isSelect = determineQueryType(query.getQueryText()).equals("SELECT");
            Map<String, String> queryParams = null;

            Object queryResult;
            int affected_rows;

            logger.info("Executing query on sourceId: {}", sourceId);
            logger.info("SQL Query: {}", query);

            DataSource dataSource = sourceService.getDataSourceById(sourceId);
            if (dataSource == null) {
                logger.error("Source not found for sourceId: {}", sourceId);
                response.put(""+i++, "Source not found: " + sourceId);
                continue;
            }

            try {
                queryParams = new ObjectMapper().readValue(query.getQueryParams(), new TypeReference<HashMap<String, String>>() {});

                String paramValueValid = validateParameters(queryParams, parameterValues);
                if (paramValueValid != null) {
                    response.put(""+i++, paramValueValid);
                    continue;
                }

                String finalQuery = replaceParameters(query.getQueryText(), parameterValues);

                logger.info("Final Query: {}", finalQuery);
                logger.info("Parameters: {}", parameterValues);

                // Create Jdbc connection with dataSource
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

                try {
                    if (isSelect) {
                        queryResult = jdbcTemplate.queryForList(finalQuery);
                    } else {
                        affected_rows = jdbcTemplate.update(finalQuery);
                        queryResult = Collections.singletonMap("updateCount", affected_rows);
                    }

                    logger.info("Query Result: {}", queryResult);
                    response.put(""+i++, queryResult);

                } catch (Exception e) {
                    logger.error("Error executing query: {}", e.getMessage());
                    response.put(""+i++,"Error executing query: " + e.getMessage());
                }

            } catch (JsonProcessingException e) {
                response.put(""+i++, "Error: processing parameters for query " + sqlMd5 + ": " + e.getMessage());
            } catch (Exception e) {
                response.put(""+i++, "Error: saving query " + sqlMd5 + ": " + e.getMessage());
            }
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    private String validateParameters(Map<String, String> queryParams, Map<String, Object> requestParams){
        for(Map.Entry<String, String> entry : queryParams.entrySet()) {
            String paramName = entry.getKey();
            String expectedType = entry.getValue();
            Object paramValue = requestParams.get(paramName);

            if (paramValue == null) throw new IllegalArgumentException("Parameter " + paramName + " is missing");

            boolean isValid = switch (expectedType) {
                case "char" -> paramValue instanceof String;
                case "integer" -> paramValue instanceof Integer;
                case "date" -> paramValue instanceof Date || isValidDate(paramValue.toString());
                case "datetime" -> paramValue instanceof Date || isValidDateTime(paramValue.toString());
                default -> false;
            };

            if (!isValid) {
                return "Error: parameter " + paramName + " is of type " + paramValue.getClass().getSimpleName() + " but expected " + expectedType;
            }
        }
        return null;
    }

    private boolean isValidDate(String datestr) {
        try {
            new SimpleDateFormat("yyyy-MM-dd").parse(datestr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isValidDateTime(String datetimestr) {
        try {
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(datetimestr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
