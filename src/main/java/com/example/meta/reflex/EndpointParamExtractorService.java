package com.example.meta.reflex;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.example.meta.reflex.model.EndpointParamInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EndpointParamExtractorService {

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private ObjectMapper objectMapper;

    public List<EndpointParamInfo> extractAllEndpointParams() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        List<EndpointParamInfo> result = new ArrayList<>();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod handler = entry.getValue();

            Set<String> paths = info.getPatternValues();
            Set<RequestMethod> methods = info.getMethodsCondition().getMethods();

            for (String path : paths) {
                //for (RequestMethod httpMethod : (methods.isEmpty() ? Set.of(RequestMethod.GET) : methods)) {
                for (RequestMethod httpMethod : (methods.isEmpty() ? new HashSet<>(Arrays.asList(RequestMethod.POST)) : methods)) { 

                    EndpointParamInfo endpointInfo = new EndpointParamInfo();
                    endpointInfo.setPath(path);
                    endpointInfo.setHttpMethod(httpMethod.name());
                    endpointInfo.setController(handler.getBeanType().getSimpleName());
                    endpointInfo.setMethod(handler.getMethod().getName());

                    Map<String, JsonNode> paramSamples = new LinkedHashMap<>();
                    JsonNode requestBodyJson = null;

                    for (Parameter param : handler.getMethod().getParameters()) {

                        if (param.isAnnotationPresent(RequestBody.class)) {
                            Object sampleObj = createSampleInstance(param.getType());
                            requestBodyJson = objectMapper.valueToTree(sampleObj);
                        } else if (param.isAnnotationPresent(RequestParam.class)) {
                            RequestParam annot = param.getAnnotation(RequestParam.class);
                            String name = annot.name().isEmpty() ? param.getName() : annot.name();
                            paramSamples.put(name, generateSampleValue(param.getType()));
                        } else if (param.isAnnotationPresent(PathVariable.class)) {
                            PathVariable annot = param.getAnnotation(PathVariable.class);
                            String name = annot.name().isEmpty() ? param.getName() : annot.name();
                            paramSamples.put("path:" + name, generateSampleValue(param.getType()));
                        } else if (param.isAnnotationPresent(RequestHeader.class)) {
                            RequestHeader annot = param.getAnnotation(RequestHeader.class);
                            String name = annot.name().isEmpty() ? param.getName() : annot.name();
                            paramSamples.put("header:" + name, generateSampleValue(param.getType()));
                        }
                    }

                    endpointInfo.setRequestParams(paramSamples);
                    endpointInfo.setRequestBody(requestBodyJson);

                    result.add(endpointInfo);
                }
            }
        }

        return result;
    }

    private Object createSampleInstance(Class<?> clazz) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                field.set(instance, getSamplePrimitive(field.getType(), field.getName()));
            }

            return instance;
        } catch (Exception e) {
            return "Error: Cannot instantiate " + clazz.getSimpleName();
        }
    }

    private Object getSamplePrimitive(Class<?> type, String nameHint) {
        if (type == String.class) return nameHint + "_value";
        if (type == int.class || type == Integer.class) return 1;
        if (type == long.class || type == Long.class) return 100L;
        if (type == boolean.class || type == Boolean.class) return true;
        if (type.isEnum()) return type.getEnumConstants()[0];
        return null;
    }

    private JsonNode generateSampleValue(Class<?> type) {
        Object sample = getSamplePrimitive(type, type.getSimpleName().toLowerCase());
        return objectMapper.valueToTree(sample);
    }
}
