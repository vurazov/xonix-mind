package com.lineate.xonix.mind.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;


@Configuration
public class WebConfig {

    @Bean
    public Gson getGson() {
        return new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                Instant instant = Instant.ofEpochMilli(json.getAsJsonPrimitive().getAsLong());
                return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            }
        }).create();
    }
}
