package com.boctool.webservice_engine.service;

import com.boctool.webservice_engine.controller.RequestController;
import com.boctool.webservice_engine.entity.*;
import com.boctool.webservice_engine.repository.QueryRepository;
import com.boctool.webservice_engine.repository.RequestRepository;
import com.boctool.webservice_engine.repository.ResponseRepository;
import com.boctool.webservice_engine.repository.SourceRepository;
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

import static com.boctool.webservice_engine.utils.Utilities.*;

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

    @Autowired
    SourceRepository sourceRepository;

    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Request> findAllRequests() {
        return requestRepository.findAll();
    }

    public ResponseEntity<Object> executeListQueries(List<RequestDTO> requestDTOS) {
        Map<String, Object> log = new HashMap<>();
        List<ResponseDTO> responseDTOs = new ArrayList<>();
        int i = 0;  // Initialize index for logging

        for (RequestDTO requestDTO : requestDTOS) {
            ResponseDTO responseDTO = executeQuery(requestDTO, i);  // Return ResponseDTO for each query
            responseDTOs.add(responseDTO);
            log.put("Query_" + i++, responseDTO.getMessage());
        }

        // Return the accumulated responses and log
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    // Execute a single query and return ResponseDTO
    public ResponseDTO executeQuery(RequestDTO requestDTO, int i) {
        ResponseDTO responseDTO = new ResponseDTO();
        String queryId = requestDTO.getSqlId();
        String sourceId = requestDTO.getSourceId();
        Map<String, Object> requestParams = requestDTO.getParameters();
        Map<String, String> queryParams = null;
        Object queryResult = null;
        List<Map<String, Object>> resultList = null;
        int affectedRows;

        String finalQuery = null;

        try {
            // Validate inputs
            validateElementsForExecution(queryId, sourceId, requestParams);

            // Fetch query details from the repository
            Query query = queryRepository.findQueryByQueryId(queryId);
            logger.info("Executing query: {}", query);

            // Determine if it's a SELECT query
            boolean isSelect = determineQueryType(query.getQueryText()).equals("SELECT");

            // Get the datasource and set up JdbcTemplate
            DataSource dataSource = sourceService.getDataSourceById(sourceId);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            // Parse and validate parameters
            queryParams = objectMapper.readValue(query.getQueryParams(), new TypeReference<HashMap<String, String>>() {
            });
            validateParameters(queryParams, requestParams);

            // Build the final query
            finalQuery = replaceParameters(query.getQueryText(), requestParams);
            logger.info("Final Query: {}", finalQuery);
            logger.info("Parameters: {}", requestParams);

            long startTime = System.currentTimeMillis();

            // Execute the query
            if (isSelect) {
                resultList = jdbcTemplate.queryForList(finalQuery);
                affectedRows = resultList.size();
                queryResult = resultList;
                System.out.println("Result query: " + resultList);
            } else {
                affectedRows = jdbcTemplate.update(finalQuery);
                queryResult = Collections.singletonMap("updateCount", affectedRows);
            }

            long endTime = System.currentTimeMillis();
            double runtime = (endTime - startTime) / 1000.0;

            // Build the response
            responseDTO.setCode(200);
            responseDTO.setStatus("SUCCESS");
            responseDTO.setRowCount(affectedRows);
            responseDTO.setData(queryResult);
            responseDTO.setMessage("Query executed successfully");
            responseDTO.setRuntime(runtime);

            saveRequest(queryId, sourceId, requestParams, finalQuery);
            saveResponse(runtime, queryId, sourceId, query.getQueryMd5(), finalQuery, queryParams, requestParams);

        } catch (IllegalArgumentException e) {
            responseDTO.setCode(400);
            responseDTO.setStatus("FAILED");
            String message = "Error validating request: " + queryId + ": " + e.getMessage();
            responseDTO.setMessage(message);
            saveResponseError(400, queryId, sourceId, requestParams, finalQuery, message);
        } catch (JsonProcessingException e) {
            responseDTO.setCode(400);
            responseDTO.setStatus("FAILED");
            String message = "Error processing parameters for query " + queryId + ": " + e.getMessage();
            responseDTO.setMessage(message);
            saveResponseError(400, queryId, sourceId, requestParams, null, message);
        } catch (Exception e) {
            responseDTO.setCode(500);
            responseDTO.setStatus("FAILED");
            String message = "Error executing query " + queryId + ": " + e.getMessage();
            responseDTO.setMessage(message);
            saveResponseError(500, queryId, sourceId, requestParams, finalQuery, message);
        }
        return responseDTO;
    }

    // Method to save the request log in the database
    private void saveResponse(double runtime, String queryId, String sourceId, String queryMd5, String finalQuery, Map<String, String> queryParams, Map<String, Object> requestParams) {
        Response response = new Response();
        response.setResponseRuntime(runtime);
        response.setResponseCode(200);
        response.setResponseMessage("Query executed successfully");
        response.setResponseSourceId(sourceId);
        response.setResponseQueryId(queryId);
        response.setResponseQueryMd5(queryMd5);
        response.setResponseQueryText(finalQuery);
        response.setResponseQueryParams(queryParams.toString());
        response.setResponseQueryValues(requestParams.toString());
        responseRepository.save(response); // Save response in the request
    }

    private void saveResponseError(int code, String sqlId, String sourceId, Map<String, Object> requestParams, String finalQuery, String message) {
        Response response = new Response();
        response.setResponseCode(code);
        response.setResponseMessage(message);
        response.setResponseSourceId(sourceId);
        response.setResponseQueryId(sqlId);
        response.setResponseQueryText(finalQuery);
        response.setResponseQueryValues(requestParams.toString());
        responseRepository.save(response); // Save response in the request
    }

    private void saveRequest(String sqlId, String sourceId, Map<String, Object> requestParams, String finalQuery) {
        Request request = new Request();
        request.setRequestQueryId(sqlId);
        request.setRequestRegdate(new Date());
        request.setRequestSourceId(sourceId);
        request.setRequestQueryValues(requestParams.toString());
        requestRepository.save(request);
    }


    private void validateElementsForExecution(String sqlId, String sourceId, Map<String, Object> requestParams) {
        if (sqlId != null && sourceId != null && requestParams != null) {
            // Check if query exists in the database
            boolean queryExists = queryRepository.existsByQueryId(sqlId);
            logger.info("Query exists: {}", queryExists);
            if (!queryExists) {
                throw new IllegalArgumentException("Error: SQL statement does not exist in the database: " + sqlId);
            }

            // Check if source exists in the database
            boolean sourceExists = sourceRepository.existsBySourceId(sourceId);
            logger.info("Source exists: {}", sourceExists);
            if (!sourceExists) {
                throw new IllegalArgumentException("Error: Source does not exist in the database: " + sourceId);
            }
        } else {
            throw new IllegalArgumentException("Error: Got null value - sqlId = " + sqlId + ", sourceId = " + sourceId + ", requestParams = " + requestParams);
        }
    }



    private void validateParameters(Map<String, String> queryParams, Map<String, Object> requestParams){
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
                throw new IllegalArgumentException("Parameter " + paramName + " is of type " + paramValue.getClass().getSimpleName() + " but expected " + expectedType);
            }
        }
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
