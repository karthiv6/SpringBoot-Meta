package com.example.meta.reflex.model;

import com.fasterxml.jackson.databind.JsonNode;

public class EndpointSampleInfo {
    private String path;
    private String httpMethod;
    private String controller;
    private String method;
    private String requestBodyType;
    private JsonNode sampleRequestBody;

    // Getters and Setters
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public String getController() { return controller; }
    public void setController(String controller) { this.controller = controller; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getRequestBodyType() { return requestBodyType; }
    public void setRequestBodyType(String requestBodyType) { this.requestBodyType = requestBodyType; }

    public JsonNode getSampleRequestBody() { return sampleRequestBody; }
    public void setSampleRequestBody(JsonNode sampleRequestBody) { this.sampleRequestBody = sampleRequestBody; }
}
