package com.example.meta.reflex;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.example.meta.reflex.model.EndpointSampleInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class EndpointSampleService {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    public List<EndpointSampleInfo> listEndpointsWithRequestBodies() {
        RequestMappingHandlerMapping mapping = context.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> methodMap = mapping.getHandlerMethods();

        List<EndpointSampleInfo> results = new ArrayList<>();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : methodMap.entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod handler = entry.getValue();

            Set<String> paths = info.getPatternValues();
            Set<RequestMethod> methods = info.getMethodsCondition().getMethods();

            for (String path : paths) {
               // for (RequestMethod httpMethod : (methods.isEmpty() ? Set.of(RequestMethod.POST) : methods)) {
            	 for (RequestMethod httpMethod : (methods.isEmpty() ? new HashSet<>(Arrays.asList(RequestMethod.POST)) : methods)) { 

                    Method javaMethod = handler.getMethod();
                    Parameter[] parameters = javaMethod.getParameters();

                    for (Parameter parameter : parameters) {
                        if (AnnotationUtils.findAnnotation(parameter, RequestBody.class) != null) {
                            Class<?> requestType = parameter.getType();

                            Object sampleObject = createSampleInstance(requestType);
                            JsonNode node = objectMapper.valueToTree(sampleObject);
                            ObjectNode objNode = node.isObject() ? (ObjectNode) node : objectMapper.createObjectNode().put("value", node.asText());

                            //ObjectNode sampleJson = (ObjectNode)  new ObjectMapper().valueToTree(sampleObject);

                            EndpointSampleInfo summary = new EndpointSampleInfo();
                            summary.setPath(path);
                            summary.setHttpMethod(httpMethod.name());
                            summary.setController(handler.getBeanType().getSimpleName());
                            summary.setMethod(javaMethod.getName());
                            summary.setRequestBodyType(requestType.getSimpleName());
                            summary.setSampleRequestBody(objNode);

                            results.add(summary);
                        }
                    }
                }
            }
        }

        return results;
    }

    private Object createSampleInstance(Class<?> clazz) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Class<?> type = field.getType();

                if (type == String.class) {
                    field.set(instance, field.getName().toLowerCase() + "_value");
                } else if (type == int.class || type == Integer.class) {
                    field.set(instance, 1);
                } else if (type == long.class || type == Long.class) {
                    field.set(instance, 100L);
                } else if (type == boolean.class || type == Boolean.class) {
                    field.set(instance, true);
                } else if (!type.isPrimitive() && !type.getName().startsWith("java")) {
                    field.set(instance, createSampleInstance(type));
                } else {
                    field.set(instance, null);
                }
            }

            return instance;
        } catch (Exception e) {
            return "Could not instantiate " + clazz.getSimpleName();
        }
    }
}
