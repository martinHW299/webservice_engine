package com.boctool.webservice_engine.controller;

import com.boctool.webservice_engine.entity.Source;
import com.boctool.webservice_engine.service.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/source")
public class SourceController {
    @Autowired
    private SourceService sourceService;

    @GetMapping
    public List<Source> findAllSources() {
        return sourceService.findAllSources();
    }

    @GetMapping("/id/{id}")
    public Source findSourceById(@PathVariable String id) {
        return sourceService.findSourceById(id);
    }

    @GetMapping("/status/{status}")
    public List<Source> findSourcesByStatus(@PathVariable String status) {
        return sourceService.findSourcesByStatus(status);
    }

    @PostMapping
    public Map<String, Object> saveSources(@RequestBody List<Source> sources) {
        Map<String, Object> response = sourceService.saveSources(sources);
        return response;
    }

    @PostMapping("/deleteAll")
    public void deleteAllSources(){
        sourceService.deleteAllSources();
    }
}