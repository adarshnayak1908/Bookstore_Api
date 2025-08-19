Feature: User Login

  Scenario: Login with valid credentials
    When I login using valid credentials
    Then the response status should be 200

  Scenario: Login with invalid password from JSON file
    When I send a POST request to login with body "invalidPassword.json"
    Then the response status should be 400

  Scenario: Login with missing email from JSON file
    When I send a POST request to login with body "missingEmail.json"
    Then the response status should be 400

  Scenario: Login with completely invalid payload from JSON file
    When I send a POST request to login with body "invalidLoginPayload.json"
    Then the response status should be 400




