package com.boctool.webservice_engine.controller;

import com.boctool.webservice_engine.entity.Response;
import com.boctool.webservice_engine.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/response")
public class ResponseController {

    private final ResponseService responseService;

    @Autowired
    public ResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }

    @GetMapping
    public List<Response> findAllResponses() {
        return responseService.findAllResponses();
    }

    @PostMapping("/deleteAll")
    public void deleteAllResponses() {
        responseService.deleteAllResponses();
    }
}
