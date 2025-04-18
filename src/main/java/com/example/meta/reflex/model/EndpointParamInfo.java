package com.example.meta.reflex.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public class EndpointParamInfo {
    private String path;
    private String httpMethod;
    private String controller;
    private String method;
    private Map<String, JsonNode> requestParams; // param name -> sample value
    private JsonNode requestBody; // if applicable
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getHttpMethod() {
		return httpMethod;
	}
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}
	public String getController() {
		return controller;
	}
	public void setController(String controller) {
		this.controller = controller;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public Map<String, JsonNode> getRequestParams() {
		return requestParams;
	}
	public void setRequestParams(Map<String, JsonNode> requestParams) {
		this.requestParams = requestParams;
	}
	public JsonNode getRequestBody() {
		return requestBody;
	}
	public void setRequestBody(JsonNode requestBody) {
		this.requestBody = requestBody;
	}

    // Getters & Setters
    
    
}