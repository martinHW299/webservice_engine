package com.boctool.webservice_engine.controller;

import com.boctool.webservice_engine.entity.Request;
import com.boctool.webservice_engine.entity.RequestDTO;
import com.boctool.webservice_engine.service.RequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/request")
public class RequestController {

    final RequestService requestService;

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
