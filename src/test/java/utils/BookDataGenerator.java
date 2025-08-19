package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class BookDataGenerator {

    public static Map<String, Object> generateRandomBook() {
        Random random = new Random();

        Map<String, Object> book = new HashMap<>();
        book.put("name", "Book-" + UUID.randomUUID().toString().substring(0, 8));
        book.put("author", "Author-" + UUID.randomUUID().toString().substring(0, 6));
        book.put("published_year", 1950 + random.nextInt(74)); // 1950–2023
        book.put("book_summary", "This is a summary for " + UUID.randomUUID().toString().substring(0, 5));

        return book;
    }
}
