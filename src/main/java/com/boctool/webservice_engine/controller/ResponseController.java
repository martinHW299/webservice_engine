package com.boctool.webservice_engine.controller;

import com.boctool.webservice_engine.service.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.boctool.webservice_engine.utils.Utils.replaceParameters;

@RestController
@RequestMapping("api/response")
public class ResponseController {

    @Autowired
    SourceService sourceService;

    private static final Logger logger = LoggerFactory.getLogger(ResponseController.class);

    public ResponseController(SourceService sourceService) {
        this.sourceService = sourceService;
    }


    /*@PostMapping
    public ResponseEntity<Object> executeQuery(@RequestBody Map<String, Object> request) {
        String sourceId = (String) request.get("sourceId");
        String query = (String) request.get("query");
        Map<String, String> parameter = (Map<String, String>) request.get("parameter");

        boolean isSelect = query.trim().toUpperCase().startsWith("SELECT");
        Object queryResult = null;
        int affected_rows = 0;

        logger.info("Executing query on sourceId: {}", sourceId);
        logger.info("SQL Query: {}", query);

        DataSource dataSource = sourceService.getDataSourceById(sourceId);

        if (dataSource == null) {
            logger.error("Source not found for sourceId: {}", sourceId);
            return new ResponseEntity<>("Source not found", HttpStatus.NOT_FOUND);
        }

        //String finalQuery = replaceParameters(query, parameter);

        logger.info("Final Query: {}", finalQuery);
        logger.info("Parameters: {}", parameter);

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
            return new ResponseEntity<>(queryResult, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error executing query: {}", e.getMessage());
            return new ResponseEntity<>("Error executing query: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }*/
}
