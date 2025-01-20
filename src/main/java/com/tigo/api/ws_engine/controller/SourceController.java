package com.tigo.api.ws_engine.controller;

import com.tigo.api.ws_engine.entity.Source;
import com.tigo.api.ws_engine.service.SourceService;
import com.tigo.api.ws_engine.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class SourceController {

    private final SourceService sourceService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getSourceById(@PathVariable UUID id) {
        try {
            Source source = sourceService.findSourceById(id);
            return ResponseEntity.ok(source);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(404, "Source not found", null));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<Source>> getActiveSources() {
        List<Source> activeSources = sourceService.findActiveSources();
        return ResponseEntity.ok(activeSources);
    }

    @PostMapping
    public ResponseEntity<?> createSource(@RequestBody Source source) {
        return sourceService.saveSource(source);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSource(@PathVariable UUID id, @RequestBody Source updatedSource) {
        return sourceService.updateSource(id, updatedSource);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAllSources() {
        try {
            sourceService.deleteAllSources();
            return ResponseEntity.ok(new ApiResponse(200, "All sources deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(500, "Failed to delete all sources", null));
        }
    }
}