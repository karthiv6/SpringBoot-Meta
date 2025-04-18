package com.example.meta.reflex.controller;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@RestController
@RequestMapping("/api/endpoints")
public class EndpointExtractorController {

    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public EndpointExtractorController(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @GetMapping
    public List<EndpointInfo> getAllEndpoints() {
        List<EndpointInfo> endpoints = new ArrayList<>();
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();

            // Get URL patterns
            Set<String> patterns = mappingInfo.getPatternValues();
            // Get HTTP methods
            Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
            // Get request body parameter details
            Parameter requestBodyParam = getRequestBodyParameter(handlerMethod);

            for (String pattern : patterns) {
                for (RequestMethod method : methods) {
                    EndpointInfo endpoint = new EndpointInfo();
                    endpoint.setUrl(pattern);
                    endpoint.setMethod(method.name());
                    endpoint.setDummyRequest(generateDummyRequest(method, requestBodyParam));
                    endpoint.setRequestBodyType(requestBodyParam != null ? requestBodyParam.getType().getSimpleName() : null);
                    endpoints.add(endpoint);
                }
            }
        }

        return endpoints;
    }

    private Parameter getRequestBodyParameter(HandlerMethod handlerMethod) {
        for (Parameter parameter : handlerMethod.getMethod().getParameters()) {
            if (AnnotationUtils.findAnnotation(parameter, RequestBody.class) != null) {
                return parameter;
            }
        }
        return null;
    }

    private String generateDummyRequest(RequestMethod method, Parameter requestBodyParam) {
        if (method == RequestMethod.GET || method == RequestMethod.DELETE) {
            return "{}"; // No body for GET or DELETE
        }

        if (requestBodyParam == null) {
            return "{}"; // No @RequestBody annotation
        }

        // Generate a dummy JSON based on the request body type
        Class<?> paramType = requestBodyParam.getType();
        return generateDummyJson(paramType);
    }

    private String generateDummyJson(Class<?> clazz) {
        try {
            Map<String, Object> dummyData = new HashMap<>();
            for (Field field : clazz.getDeclaredFields()) {
                String fieldName = field.getName();
                Class<?> fieldType = field.getType();

                // Assign dummy values based on field type
                if (fieldType == String.class) {
                    dummyData.put(fieldName, "example" + fieldName);
                } else if (fieldType == Integer.class || fieldType == int.class) {
                    dummyData.put(fieldName, 0);
                } else if (fieldType == Double.class || fieldType == double.class) {
                    dummyData.put(fieldName, 0.0);
                } else if (fieldType == Boolean.class || fieldType == boolean.class) {
                    dummyData.put(fieldName, false);
                } else if (fieldType == List.class) {
                    dummyData.put(fieldName, Collections.singletonList("exampleItem"));
                } else {
                    dummyData.put(fieldName, null); // Fallback for complex types
                }
            }

            // Convert to JSON string
            return toJsonString(dummyData);
        } catch (Exception e) {
            return "{\"error\": \"Unable to generate dummy JSON\"}";
        }
    }

    private String toJsonString(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            json.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof List) {
                json.append("[\"").append(((List<?>) value).get(0)).append("\"]");
            } else {
                json.append(value == null ? "null" : value);
            }
            if (iterator.hasNext()) {
                json.append(",");
            }
        }
        json.append("}");
        return json.toString();
    }

    static class EndpointInfo {
        private String url;
        private String method;
        private String dummyRequest;
        private String requestBodyType;

        // Getters and setters
        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getDummyRequest() {
            return dummyRequest;
        }

        public void setDummyRequest(String dummyRequest) {
            this.dummyRequest = dummyRequest;
        }

        public String getRequestBodyType() {
            return requestBodyType;
        }

        public void setRequestBodyType(String requestBodyType) {
            this.requestBodyType = requestBodyType;
        }
    }
}