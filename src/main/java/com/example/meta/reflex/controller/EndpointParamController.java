package com.example.meta.reflex.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.reflex.EndpointParamExtractorService;
import com.example.meta.reflex.model.EndpointParamInfo;

@RestController
@RequestMapping("/meta")
public class EndpointParamController {

    @Autowired
    private EndpointParamExtractorService service;

    @GetMapping("/endpoints")
    public List<EndpointParamInfo> getAllEndpointParamSamples() {
        return service.extractAllEndpointParams();
    }
}
