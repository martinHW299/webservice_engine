package com.boctool.webservice_engine.service;

import com.boctool.webservice_engine.controller.RequestController;
import com.boctool.webservice_engine.entity.*;
import com.boctool.webservice_engine.repository.QueryRepository;
import com.boctool.webservice_engine.repository.RequestRepository;
import com.boctool.webservice_engine.repository.ResponseRepository;
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

    @Autowired
    ResponseRepository responseRepository;

    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);

    public RequestService(RequestRepository requestRepository, QueryRepository queryRepository, SourceService sourceService, ResponseRepository responseRepository) {
        this.requestRepository = requestRepository;
        this.queryRepository = queryRepository;
        this.sourceService = sourceService;
        this.responseRepository = responseRepository;
    }


    public List<Request> findAllRequests() {
        return requestRepository.findAll();
    }

    public ResponseEntity<Object> executeQueries(List<RequestDTO> requestDTOS) {
        Map<String, Object> log = new HashMap<>();
        int i = 1;

        for (RequestDTO requestDTO : requestDTOS) {
            String sqlMd5 = requestDTO.getSql();
            String sourceId = requestDTO.getSource();
            Map<String, Object> parameterValues = requestDTO.getParameters();

            Query query = queryRepository.findQueryByQueryMd5(sqlMd5);
            if (query == null) {
                log.put(""+i++, "Error: sql statement does not exists in the database: " + sqlMd5);
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
                log.put(""+i++, "Source not found: " + sourceId);
                continue;
            }

            Response response = new Response();
            try {
                queryParams = new ObjectMapper().readValue(query.getQueryParams(), new TypeReference<HashMap<String, String>>() {});

                // Create Jdbc connection with dataSource
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

                try {
                    String paramValueValid = validateParameters(queryParams, parameterValues);
                    if (paramValueValid != null) {
                        log.put(""+i++, paramValueValid);
                        continue;
                    }

                    String finalQuery = replaceParameters(query.getQueryText(), parameterValues);

                    logger.info("Final Query: {}", finalQuery);
                    logger.info("Parameters: {}", parameterValues);

                    long startTime = System.currentTimeMillis();
                    if (isSelect) {
                        queryResult = jdbcTemplate.queryForList(finalQuery);
                    } else {
                        affected_rows = jdbcTemplate.update(finalQuery);
                        queryResult = Collections.singletonMap("updateCount", affected_rows);
                    }
                    long endTime = System.currentTimeMillis();

                    logger.info("Query Result: {}", queryResult);
                    log.put(""+i++, queryResult);

                    Request request = new Request();
                    request.setRequestQueryId(sqlMd5);
                    request.setRequestRegdate(new Date());
                    request.setRequestSourceId(sourceId);
                    request.setRequestQueryValues(parameterValues.toString());
                    requestRepository.save(request);

                    response.setResponseRuntime((endTime - startTime) / 1000.0);
                    response.setResponseCode(200);
                    response.setResponseMessage("Query executed successfully");
                    response.setResponseData(queryResult.toString());
                    response.setResponseSourceId(sourceId);
                    response.setResponseQueryId(query.getQueryId());
                    response.setResponseQueryMd5(sqlMd5);
                    response.setResponseQueryText(finalQuery);
                    response.setResponseQueryParams(queryParams.toString());
                    response.setResponseQueryValues(parameterValues.toString());

                } catch (Exception e) {
                    logger.error("Error executing query: {}", e.getMessage());
                    log.put(""+i++,"Error executing query: " + e.getMessage());

                    response.setResponseCode(500);
                    response.setResponseMessage(log.toString());
                    response.setResponseData(null);
                }

                responseRepository.save(response);



            } catch (JsonProcessingException e) {
                log.put(""+i++, "Error: processing parameters for query " + sqlMd5 + ": " + e.getMessage());
            } catch (Exception e) {
                log.put(""+i++, "Error: saving query " + sqlMd5 + ": " + e.getMessage());
            }

        }
        return new ResponseEntity<>(log, HttpStatus.OK);
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

    public void deleteAllRequests(){
        requestRepository.deleteAll();
    }
}
