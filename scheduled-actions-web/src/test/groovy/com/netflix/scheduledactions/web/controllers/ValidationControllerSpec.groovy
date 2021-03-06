package com.netflix.scheduledactions.web.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.test.web.servlet.MvcResult
import spock.lang.Shared
import spock.lang.Specification
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class ValidationControllerSpec extends Specification {

  @Shared ObjectMapper objectMapper = new ObjectMapper()

  @Shared def mvc = MockMvcBuilders.standaloneSetup(new ValidationController()).build()

  MvcResult validate(String expression) {
    mvc.perform(MockMvcRequestBuilders
        .get("/validateCronExpression")
        .param("cronExpression", expression)).andReturn()
  }

  void 'should include description when expression is valid'() {
    when:
    def result = validate("0 0 10 ? * 1")

    def responseBody = objectMapper.readValue(result.response.contentAsByteArray, Map)

    then:
    responseBody.response == "Cron expression is valid"
    responseBody.description == "At 10:00 AM, only on Sunday"
  }

  void 'should include failure message when expression is invalid'() {
    when:
    def result = validate("0 0 10 * * 1")

    then:
    result.response.status == 400
    result.response.errorMessage == "Cron expression '0 0 10 * * 1' is not valid: Support for specifying both a " +
        "day-of-week AND a day-of-month parameter is not implemented."
  }
}
