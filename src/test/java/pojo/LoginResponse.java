package pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a login response payload from the API.
 * Stores the JWT access token returned upon successful login.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    private String access_token;
    private Integer expires_in;

    public String getAccess_token() { return access_token; }
    public void setAccess_token(String access_token) { this.access_token = access_token; }
    public Integer getExpiresIn() { return expires_in; }
}
