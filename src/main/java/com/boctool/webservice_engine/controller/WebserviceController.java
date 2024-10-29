package com.boctool.webservice_engine.controller;

import com.boctool.webservice_engine.entity.Webservice;
import com.boctool.webservice_engine.entity.WebserviceDTO;
import com.boctool.webservice_engine.service.WebserviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/ws")
public class WebserviceController {

    final WebserviceService webserviceService;

    public WebserviceController(WebserviceService webserviceService) {
        this.webserviceService = webserviceService;
    }

    @GetMapping("/find")
    public List<Webservice> findAllWebservices() {
        return webserviceService.findAllWebservices();
    }

    @GetMapping("/find/{id}")
    public Optional<Webservice> findByWebserviceId(@PathVariable String id) {
        return webserviceService.findByWebserviceId(id);
    }

    @PostMapping
    public ResponseEntity<Object> saveWebservices(@RequestBody List<WebserviceDTO> webserviceDTOS) {
        return webserviceService.saveWebservices(webserviceDTOS);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateWebservice(@PathVariable String id, @RequestBody WebserviceDTO webserviceDTO) {
        return webserviceService.updateWebservice(id, webserviceDTO);
    }


    @PostMapping("/deleteAll")
    public void deleteAllWebservices() {
        webserviceService.deleteAllWebservices();
    }

}
