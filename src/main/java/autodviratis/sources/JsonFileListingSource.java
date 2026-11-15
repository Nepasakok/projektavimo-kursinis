package autodviratis.sources;

import autodviratis.model.RawListing;
import autodviratis.model.Tenant;
import autodviratis.service.ListingSource;
import autodviratis.service.SourceUnavailableException;
import autodviratis.utils.JsonFileReader;
import autodviratis.utils.JsonListing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonFileListingSource implements ListingSource {

    private static final String DATA_FOLDER = "data/";
    private static final String FILE_EXTENSION = ".json";

    private final String name;
    private final String fileName;
    private final JsonFileReader jsonFileReader;

    public JsonFileListingSource(String name, String fileName, JsonFileReader jsonFileReader) {
        this.name = name;
        this.fileName = fileName;
        this.jsonFileReader = jsonFileReader;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<RawListing> fetch(Tenant tenant) throws SourceUnavailableException {
        String path = DATA_FOLDER + tenant.getId() + "/" + fileName + FILE_EXTENSION;
        List<JsonListing> jsonListings;
        try {
            jsonListings = jsonFileReader.readListings(path);
        } catch (IOException e) {
            throw new SourceUnavailableException(name + ": nepavyko nuskaityti " + path);
        }
        List<RawListing> result = new ArrayList<>();
        for (JsonListing json : jsonListings) {
            if (json.price() == null) {
                throw new SourceUnavailableException(name + ": skelbimas be kainos faile " + path);
            }
            result.add(new RawListing(name, json.title(), json.description(), json.price(),
                    json.city(), json.photoHash(), json.url()));
        }
        return result;
    }
}
