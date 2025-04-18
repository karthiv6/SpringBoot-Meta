package com.example.meta.reflex.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.meta.reflex.EndpointSampleService;
import com.example.meta.reflex.model.EndpointSampleInfo;

@RestController
@RequestMapping("/services")
public class EndpointSampleController {

    @Autowired
    private EndpointSampleService endpointSampleService;

    @GetMapping("/sample-requests")
    public List<EndpointSampleInfo> getSampleRequestBodies() {
        return endpointSampleService.listEndpointsWithRequestBodies();
    }
}
