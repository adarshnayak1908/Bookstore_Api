package pojo;

/**
 * Represents a login request payload for the API.
 * Contains user ID, email, and password fields.
 */
public class LoginRequest {
    private int id;
    private String email;
    private String password;

    /** Default constructor for serialization/deserialization. */
    public LoginRequest() {}

    /**
     * Constructs a LoginRequest with all fields.
     *
     * @param id       User ID
     * @param email    User email
     * @param password User password
     */
    public LoginRequest(int id, String email, String password) {
        this.id = id;
        this.email = email;
        this.password = password;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
