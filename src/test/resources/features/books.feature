Feature: Books API

  Scenario: Create a book with valid data
    When I send a POST request to books with "RANDOM" and JWT
    Then the response status should be 200

  Scenario: Create a book without name
    When I send a POST request to books with "missingTitleBook.json" and JWT
    Then the response status should be 400

  Scenario: Get all books 
    When I send a GET request to books
    Then the response status should be 200
    And the response should contain at least 1 book
    And the first book should have a name and author

  Scenario: Get a book by valid ID
    When I send a GET request to books with id 12
    Then the response status should be 200

  Scenario: Get a book by invalid ID
    When I send a GET request to books with id 999999
    Then the response status should be 404

  Scenario: Update a book with valid data
    When I send a PUT request to books with id 12 using "updateBook.json"
    Then the response status should be 200

  Scenario: Update a book with missing name
    When I send a PUT request to books with id 12 using "missingTitleBook.json"
    Then the response status should be 400

  Scenario: Delete a book by invalid ID
    When I send a DELETE request to books with id 999999
    Then the response status should be 404