package com.threedagpl.framework.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.TimeZone;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * 程序注解配置
 *
 * @author ruoyi
 */
@Configuration
// 表示通过 aop 框架暴露该代理对象，AopContext 能够访问
@EnableAspectJAutoProxy(exposeProxy = true)
// 指定要扫描的 Mapper 类的包的路径
@MapperScan("com.threedagpl.**.mapper")
public class ApplicationConfig
{
    /**
     * 时区配置
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization()
    {
        return jacksonObjectMapperBuilder -> {
            jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());
            // 允许 JSON 中存在单引号
            jacksonObjectMapperBuilder.featuresToEnable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
            // 允许 JSON 中存在未加引号的字段名
            jacksonObjectMapperBuilder.featuresToEnable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        };
    }

    /**
     * 配置 ObjectMapper 以更好地处理富文本内容
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 允许单引号
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 允许未加引号的字段名
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 忽略未知字段
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 禁用将日期写为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 设置时区
        objectMapper.setTimeZone(TimeZone.getDefault());
        return objectMapper;
    }

    /**
     * 配置消息转换器以支持富文本内容
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }
}
