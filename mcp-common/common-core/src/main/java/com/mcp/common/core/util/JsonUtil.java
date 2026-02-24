package com.mcp.common.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JSON 工具类
 *
 * @author MCP Platform
 * @since 1.0.0
 */
@Slf4j
public final class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 注册 Java 8 时间模块
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        // 禁用序列化时写入时间戳
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 忽略未知字段
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 空对象序列化不失败
        OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    private JsonUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 对象转 JSON 字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转 JSON 失败：{}", obj, e);
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }

    /**
     * JSON 字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON 转对象失败：{}", json, e);
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    /**
     * JSON 字符串转 List
     */
    public static <T> List<T> toList(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(json, 
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            log.error("JSON 转 List 失败：{}", json, e);
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    /**
     * JSON 字符串转 Map
     */
    public static Map<String, Object> toMap(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON 转 Map 失败：{}", json, e);
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    /**
     * JSON 字符串转指定类型引用
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("JSON 转对象失败：{}", json, e);
            throw new RuntimeException("JSON 反序列化失败", e);
        }
    }

    /**
     * 对象转 Pretty JSON 字符串
     */
    public static String toPrettyJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转 Pretty JSON 失败：{}", obj, e);
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }

    /**
     * 获取 ObjectMapper 实例
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}
