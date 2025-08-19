package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utility class for reading JSON files from the resources directory.
 */
public class JsonFileReader {

    private static final String BASE_PATH = "src/test/resources/testData/";

    /**
     * Reads a JSON file from the default resources/json directory.
     *
     * @param fileName Name of the JSON file (e.g., "loginRequest.json")
     * @return The file content as a String
     * @throws IOException If the file cannot be read
     */
    public static String readJsonFromFile(String fileName) throws IOException {
        String filePath = BASE_PATH + fileName;
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
}
