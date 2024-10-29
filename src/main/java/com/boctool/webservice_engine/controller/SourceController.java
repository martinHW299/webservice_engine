package com.boctool.webservice_engine.controller;

import com.boctool.webservice_engine.entity.Source;
import com.boctool.webservice_engine.service.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/source")
public class SourceController {

    private final SourceService sourceService;

    @Autowired
    public SourceController(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    @GetMapping
    public List<Source> findAllSources() {
        return sourceService.findAllSources();
    }

    @GetMapping("/id")
    public Optional<Source> findSourceById(@PathVariable String id) {
        return sourceService.findSourceById(id);
    }

    @GetMapping("/status")
    public List<Source> findSourcesByStatus(@PathVariable String status) {
        return sourceService.findSourcesByStatus(status);
    }

    @PostMapping
    public void saveSources(@RequestBody List<Source> sources) {
        sourceService.saveListOfSources(sources);
    }

    @PostMapping("/deleteAll")
    public void deleteAllSources() {
        sourceService.deleteAllSources();
    }
}