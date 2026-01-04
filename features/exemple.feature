Feature: API Test
  Scenario: Test basic functionality
    Given the API is running
    When I call the getMessage method
    Then I should receive "Hello from API"