package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import jakarta.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Configuración para manejo de zonas horarias y serialización de fechas
 */
@Configuration
public class TimeZoneConfig {

    /**
     * Establece la zona horaria por defecto de la aplicación
     */
    @PostConstruct
    public void init() {
        // Establecer la zona horaria por defecto para toda la aplicación
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
    }

    /**
     * Configuración personalizada de Jackson para manejo de fechas
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .timeZone(TimeZone.getTimeZone("America/Bogota"))
                .simpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .serializers(new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
}
