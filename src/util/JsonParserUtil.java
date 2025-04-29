package src.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonParserUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T fromJson(String jsonString, Class<T> clazz) throws IOException {
        return mapper.readValue(jsonString, clazz);
    }

    public static <T> T fromJson(String jsonString, TypeReference<T> typeReference) throws IOException {
        return mapper.readValue(jsonString, typeReference);
    }

    public static String toJson(Object obj) throws IOException {
        return mapper.writeValueAsString(obj);
    }
}
