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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.boctool.webservice_engine.utils.Utilities.*;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final QueryRepository queryRepository;
    private final SourceService sourceService;
    private final ResponseRepository responseRepository;
    private final SourceRepository sourceRepository;

    @Autowired
    public RequestService(RequestRepository requestRepository, QueryRepository queryRepository, SourceService sourceService, ResponseRepository responseRepository, SourceRepository sourceRepository) {
        this.requestRepository = requestRepository;
        this.queryRepository = queryRepository;
        this.sourceService = sourceService;
        this.responseRepository = responseRepository;
        this.sourceRepository = sourceRepository;
    }

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
            validateElementsForExecution(queryId, sourceId, requestParams);

            Query query = queryRepository.findQueryByQueryId(queryId);
            logger.info("Executing query: {}", query);

            boolean isSelect = determineQueryType(query.getQueryText()).equals("SELECT");

            DataSource dataSource = sourceService.getDataSourceById(sourceId);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            queryParams = objectMapper.readValue(query.getQueryParams(), new TypeReference<HashMap<String, String>>() {
            });
            validateParameters(queryParams, requestParams);

            finalQuery = replaceParameters(query.getQueryText(), requestParams);
            logger.info("Final Query: {}", finalQuery);
            logger.info("Parameters: {}", requestParams);

            long startTime = System.currentTimeMillis();

            if (isSelect) {
                resultList = jdbcTemplate.queryForList(finalQuery);
                affectedRows = resultList.size();
                queryResult = lowerCaseJsonKey(resultList);
                logger.info("Result query: {}", resultList);
            } else {
                affectedRows = jdbcTemplate.update(finalQuery);
                queryResult = Collections.singletonMap("updateCount", affectedRows);
            }

            long endTime = System.currentTimeMillis();
            double runtime = (endTime - startTime) / 1000.0;

            responseDTO.setCode(200);
            responseDTO.setStatus("SUCCESS");
            responseDTO.setRowCount(affectedRows);
            responseDTO.setRuntime(runtime);
            responseDTO.setData(queryResult);
            responseDTO.setMessage("Query executed successfully");

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


    private void validateElementsForExecution(String sqlId, String sourceId, Map<String, Object> requestParams) {
        if (sqlId != null && sourceId != null && requestParams != null) {
            // Check if query exists in the database
            boolean queryExists = queryRepository.existsByQueryId(sqlId);
            logger.info("Query exists: {}", queryExists);
            if (!queryExists) {
                throw new IllegalArgumentException("Error SQL statement does not exist in the database: " + sqlId);
            }
            // Check if source exists in the database
            boolean sourceExists = sourceRepository.existsBySourceId(sourceId);
            logger.info("Source exists: {}", sourceExists);
            if (!sourceExists) {
                throw new IllegalArgumentException("Error source does not exist in the database: " + sourceId);
            }
        } else {
            throw new IllegalArgumentException("Error got null value - sqlId = " + sqlId + ", sourceId = " + sourceId + ", requestParams = " + requestParams);
        }
    }

    private String replaceParameters(String query, Map<String, Object> parameters) {
        Pattern pattern = Pattern.compile(REGEX_BRACE_PATTER);
        Matcher matcher = pattern.matcher(query);
        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            String paramName = matcher.group(1);
            Object paramValue = parameters.get(paramName);

            if (paramValue != null) {
                String paramValueStr;

                if (paramValue instanceof List<?> paramList) {
                    if (!paramList.isEmpty() && paramList.get(0) instanceof String) {
                        paramValueStr = paramList.stream().map(item -> "'" + item.toString() + "'")  // Wrap in quotes
                                .reduce((a, b) -> a + ", " + b).orElse("");
                    } else {
                        // Handle array_integer: no quotes, just join with commas
                        paramValueStr = paramList.stream().map(Object::toString).reduce((a, b) -> a + ", " + b).orElse("");
                    }
                } else if (paramValue instanceof Number) {
                    paramValueStr = paramValue.toString();
                } else if (isFunction(paramValue.toString())) {
                    paramValueStr = paramValue.toString();
                } else {
                    paramValueStr = "'" + paramValue.toString() + "'";
                }

                matcher.appendReplacement(builder, paramValueStr);
            } else {
                throw new RuntimeException("Parameter " + paramName + " not found in provided parameters");
            }
        }

        matcher.appendTail(builder);
        return builder.toString();
    }


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
        response.setResponseMessage(null);
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

    private void validateParameters(Map<String, String> queryParams, Map<String, Object> requestParams) {

        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            String paramName = entry.getKey();
            String expectedType = entry.getValue();
            Object paramValue = requestParams.get(paramName);

            if (paramValue == null) throw new IllegalArgumentException("Parameter " + paramName + " is missing");

            boolean isValid = switch (expectedType) {
                case "char", "function" -> paramValue instanceof String;
                case "integer" -> paramValue instanceof Integer || paramValue instanceof Long;
                case "date" -> paramValue instanceof Date || isValidDate(paramValue.toString());
                case "datetime" -> paramValue instanceof Date || isValidDateTime(paramValue.toString());
                case "array_char" ->
                        paramValue instanceof List<?> && allElementsAreOfType((List<?>) paramValue, String.class);
                case "array_integer" ->
                        paramValue instanceof List<?> && allElementsAreOfType((List<?>) paramValue, Integer.class);
                default -> false;
            };

            if (!isValid) {
                throw new IllegalArgumentException("Parameter " + paramName + " is of type " + paramValue.getClass().getSimpleName() + " but expected " + expectedType);
            }
        }
    }

    private boolean isValidDate(String datestr) {
        try {
            new SimpleDateFormat("dd-MM-yyyy").parse(datestr);
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


//    private boolean isFunction(String value) {
//        List<String> knownFunctions = List.of("TRUNC", "TO_DATE", "ADD_MONTHS", "F_USER_ID");
//
//        for (String function : knownFunctions) {
//            if (value.toUpperCase().startsWith(function.toUpperCase() + "(") && value.endsWith(")")) {
//                return true;
//            }
//        }
//        return value.matches("^[a-zA-Z_]+\\(.*\\)$");
//    }

    private boolean isFunction(String value) {
        String functionPattern = "^[a-zA-Z_]+\\(.*\\)$";
        return value.matches(functionPattern);
    }


    private boolean allElementsAreOfType(List<?> list, Class<?> expectedType) {
        for (Object element : list) {
            if (!expectedType.isInstance(element)) {
                return false;
            }
        }
        return true;
    }

    public void deleteAllRequests() {
        requestRepository.deleteAll();
    }
}
