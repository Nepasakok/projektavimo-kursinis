package autodviratis.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class JsonFileReader {

    private final ObjectMapper objectMapper;

    public JsonFileReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<JsonListing> readListings(String resourcePath) throws IOException {
        try (InputStream input = JsonFileReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new FileNotFoundException("Failas nerastas: " + resourcePath);
            }
            return List.of(objectMapper.readValue(input, JsonListing[].class));
        }
    }
}
