package org.hikingdev.microsoft_hackathon.util.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.locationtech.jts.geom.LineString;

import java.time.LocalDateTime;

public class SerializationMapper extends ObjectMapper {
    public SerializationMapper(){
        SimpleModule module = new SimpleModule();
        module.addSerializer(LineString.class, new LineStringSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeAdapter.LocalDateTimeDeserializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeAdapter.LocalDateTimeSerializer());
        this.registerModule(module);
    }
}
