package utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import pojo.LoginRequest;
import pojo.LoginResponse;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

public final class TokenManager {

    private static final AtomicReference<String> TOKEN = new AtomicReference<>(null);
    private static volatile long expiryEpochSeconds = 0L;

    // Load from config.properties (path can be adjusted if needed)
    private static final ConfigReader CFG = new ConfigReader("src/test/resources/config.properties");
    private static final String BASE_URI = CFG.get("base_uri");
    // default to /login (your steps use /login). Override via -Dapi.login.path=/auth/login if needed
    private static final String LOGIN_PATH = System.getProperty("api.login.path", "/login");

    private static final String EMAIL = CFG.get("email");
    private static final String PASSWORD = CFG.get("password");
    private static final String ID_STR = CFG.get("id");

    private TokenManager() {}

    /** Get a valid token, refreshing if needed (thread-safe). */
    public static String getToken() {
        String tk = TOKEN.get();
        if (tk == null || isExpired()) {
            synchronized (TokenManager.class) {
                tk = TOKEN.get();
                if (tk == null || isExpired()) {
                    refreshToken();
                }
            }
        }
        return TOKEN.get();
    }

    /** Force-refresh the token (used after a 401). */
    public static void forceRefresh() {
        synchronized (TokenManager.class) {
            refreshToken();
        }
    }

    /** Warm up at suite start, but don’t fail the whole run if login is misconfigured. */
    public static void prewarm() {
        try {
            getToken();
        } catch (Throwable t) {
            System.out.println("[Auth] Prewarm failed: " + t.getMessage() + " (will retry on first request)");
        }
    }

    private static boolean isExpired() {
        return Instant.now().getEpochSecond() >= expiryEpochSeconds;
    }

    private static void refreshToken() {
        // Build login payload from your POJO
        LoginRequest req = new LoginRequest();
        try {
            if (ID_STR != null && !ID_STR.isBlank()) {
                req.setId(Integer.parseInt(ID_STR.trim()));
            }
        } catch (NumberFormatException ignored) {}
        req.setEmail(EMAIL);
        req.setPassword(PASSWORD);

        System.out.println("[Auth] POST " + BASE_URI + LOGIN_PATH);

        Response r = RestAssured.given()
                .baseUri(BASE_URI)
                .contentType(ContentType.JSON)
                .body(req)
                .when()
                .post(LOGIN_PATH);

        int code = r.getStatusCode();
        if (code != 200) {
            throw new IllegalStateException("Login failed: HTTP " + code + " — check base_uri/login path/credentials");
        }

        LoginResponse resp = r.then().extract().as(LoginResponse.class);

        // Adjust these to match your LoginResponse getters
        String token = resp.getAccess_token();
        Integer expiresIn = null;
        try {
            expiresIn = resp.getExpiresIn();
        } catch (Exception ignored) { /* not provided */ }

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("LoginResponse did not contain a token");
        }

        TOKEN.set(token);
        long ttl = (expiresIn != null && expiresIn > 0) ? expiresIn : (55 * 60); // ~55 min default
        expiryEpochSeconds = Instant.now().getEpochSecond() + ttl - 15;          // small buffer
        System.out.println("[Auth] Token refreshed; TTL ~" + ttl + "s");
    }
}
