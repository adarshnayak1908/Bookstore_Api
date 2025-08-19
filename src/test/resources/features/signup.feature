Feature: User Signup

  Scenario: Signup with valid credentials
    When I send a POST request to signup with body "SignUp.json"
    Then the response status should be 200

  Scenario: Signup with missing password
    When I send a POST request to signup with body "invalidData.json"
    Then the response status should be 422