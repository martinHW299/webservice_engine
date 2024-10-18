package com.boctool.webservice_engine.controller;

import com.boctool.webservice_engine.entity.Query;
import com.boctool.webservice_engine.entity.QueryDTO;
import com.boctool.webservice_engine.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.boctool.webservice_engine.utils.Utilities.strToMap;


@RestController
@RequestMapping("api/query")
public class QueryController {

    private final QueryService queryService;

    @Autowired
    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    public List<Query> findAllSources() {
        return queryService.findAllQueries();
    }

    @GetMapping("/id/{id}")
    public Query findQueryById(@PathVariable String id){
        return queryService.findQueryById(id);
    }

    @GetMapping("/md5/{md5}")
    public ResponseEntity<QueryDTO> findQueryByMd5(@PathVariable String md5) {
        Query query = queryService.findQueryByMd5(md5);
        if (query == null) {
            return ResponseEntity.notFound().build();
        }
        QueryDTO queryDTO = new QueryDTO();
        queryDTO.setSql(query.getQueryText());
        queryDTO.setParameters(strToMap(query.getQueryParams()));
        return ResponseEntity.ok(queryDTO);
    }

    @PostMapping
    public ResponseEntity<Object> saveQuery(@RequestBody List<QueryDTO> queryDTOS) {
        return queryService.saveQueries(queryDTOS);
    }

    @PostMapping("/deleteAll")
    public void deleteAllQueries() {
        queryService.deleteAllQueries();
    }

}
