package com.boctool.webservice_engine.controller;

import com.boctool.webservice_engine.entity.Request;
import com.boctool.webservice_engine.entity.RequestDTO;
import com.boctool.webservice_engine.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/request")
public class RequestController {

    private final RequestService requestService;

    @Autowired
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    public List<Request> findAllRequests() {
        return requestService.findAllRequests();
    }

    @PostMapping
    public ResponseEntity<Object> executeQuery(@RequestBody List<RequestDTO> requestDTOS) {
        return requestService.executeListQueries(requestDTOS);
    }

    @PostMapping("/deleteAll")
    public void deleteAllRequests() {
        requestService.deleteAllRequests();
    }
}
