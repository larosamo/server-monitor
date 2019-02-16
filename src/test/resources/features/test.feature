@BasicSteps @Smoke @Regression
Feature: Basic Test of /servers endpoint

  @Overides @Json @RemoteServer
  Scenario: Test /dashboard view available
    Given I am a JSON API consumer
      And I am executing test "Dashboard available test"
     When I request GET "/dashboard" on "http://localhost:8080"
     Then I should get a status code of 200

  @Overides @Json @RemoteServer
  Scenario: Test /servers endpoint
    Given I am a JSON API consumer
      And I am executing test "/servers endpoint available test"
     When I request GET "/servers" on "http://localhost:8080"
     Then I should get a status code of 200 
     
  @Overides @Json @RemoteServer
  Scenario: Test /monitor endpoint
    Given I am a JSON API consumer
      And I am executing test "/monitor endpoint available test"
     When I request POST "/monitor" on "http://localhost:8080"
     Then I should get a status code of 200       
     
  @Overides @Json @RemoteServer
  Scenario: Test /add endpoint
    Given I am a JSON API consumer
      And I am executing test "/add endpoint available test"
     When I request POST "/add" on "http://localhost:8080"
      And I set the JSON body to
        """
        {"name":"Test","url":"http://localhost:8080","active":"true"}
        """
     Then I should get a status code of 200

  @Overides @Json @RemoteServer
  Scenario: Test /historic endpoint
    Given I am a JSON API consumer
      And I am executing test "/historic endpoint available test"
     When I request GET "/historic" on "http://localhost:8080"
      And I provide the parameter "serverName" with a value of "Test"
     Then I should get a status code of 200   	
     
  @Overides @Json @RemoteServer
  Scenario: Test /servers endpoint
    Given I am a JSON API consumer
      And I am executing test "/servers endpoint available test"
     When I request GET "/servers" on "http://localhost:8080"
     Then I should get a status code of 200 

  @Overides @Json @RemoteServer
  Scenario: Test /activate endpoint
    Given I am a JSON API consumer
      And I am executing test "/activate endpoint available test"
     When I request GET "/activate" on "http://localhost:8080"
      And I provide the parameter "serverName" with a value of "Test"
     Then I should get a status code of 200 	
	
  @Overides @Json @RemoteServer
  Scenario: Test /deactivate endpoint
    Given I am a JSON API consumer
      And I am executing test "/deactivate endpoint available test"
     When I request GET "/deactivate" on "http://localhost:8080"
      And I provide the parameter "serverName" with a value of "Test"
     Then I should get a status code of 200 	
     
  @Overides @Json @RemoteServer
  Scenario: Test /remove endpoint
    Given I am a JSON API consumer
      And I am executing test "/remove endpoint available test"
     When I request GET "/remove" on "http://localhost:8080"
      And I provide the parameter "serverName" with a value of "Test"
     Then I should get a status code of 200 
