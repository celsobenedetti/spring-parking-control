package com.api.parkingcontrol.configs;

import java.time.format.DateTimeFormatter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

public class DateConfig {
    public static final String DATE_FORMAT = "yyyy-MM-dd'T' HH:mm:ss'Z'";
    public static final LocalDateTimeSerializer LOCAL_DATE_TIME_SERIALIZER = new LocalDateTimeSerializer(
            DateTimeFormatter.ofPattern(DATE_FORMAT));

    @Bean
    @Primary
    public ObjectMapper objectWrapper() {
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LOCAL_DATE_TIME_SERIALIZER);
        return new ObjectMapper().registerModule(module);
    }
}
