package com.boctool.webservice_engine.controller;

import com.boctool.webservice_engine.entity.Query;
import com.boctool.webservice_engine.entity.QueryDTO;
import com.boctool.webservice_engine.service.QueryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/query")
public class QueryController {

    @Autowired
    QueryService queryService;


    @GetMapping
    public List<Query> findAllSources() {
        return queryService.findAllQueries();
    }

    @PostMapping
    public ResponseEntity<Object> saveQuery(@RequestBody List<QueryDTO> queryDTOS) {
        return queryService.saveQueries(queryDTOS);
    }

    @PostMapping("/deleteAll")
    public void deleteAllQueries(){
        queryService.deleteAllQueries();
    }

}
