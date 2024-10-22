package com.boctool.webservice_engine.controller;

import com.boctool.webservice_engine.entity.Webservice;
import com.boctool.webservice_engine.entity.WebserviceDTO;
import com.boctool.webservice_engine.service.WebserviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/ws")
public class WebserviceController {

    final WebserviceService webserviceService;

    public WebserviceController(WebserviceService webserviceService) {
        this.webserviceService = webserviceService;
    }

    @GetMapping
    public List<Webservice> findAllSources() {
        return webserviceService.findAllWebservices();
    }

    @PostMapping
    public ResponseEntity<Object> saveWebservices(@RequestBody List<WebserviceDTO> webserviceDTOS) {
        return webserviceService.saveWebservice(webserviceDTOS);
    }

    @PostMapping("/deleteAll")
    public void deleteAllWebservices() {
        webserviceService.deleteAllWebservices();
    }

}
