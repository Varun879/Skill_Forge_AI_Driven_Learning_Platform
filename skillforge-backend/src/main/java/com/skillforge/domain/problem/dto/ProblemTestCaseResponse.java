package com.skillforge.domain.problem.dto;

import com.skillforge.domain.problem.entity.TestCase;

public class ProblemTestCaseResponse {

    private Long id;
    private String input;
    private String output;
    private String explanation;
    private boolean sample;

    private ProblemTestCaseResponse() {}

    public static ProblemTestCaseResponse from(TestCase testCase) {
        ProblemTestCaseResponse response = new ProblemTestCaseResponse();
        response.id = testCase.getId();
        response.input = testCase.getInputData();
        response.output = testCase.getExpectedOutput();
        response.explanation = testCase.getExplanation();
        response.sample = testCase.isSample();
        return response;
    }

    public Long getId() { return id; }
    public String getInput() { return input; }
    public String getOutput() { return output; }
    public String getExplanation() { return explanation; }
    public boolean isSample() { return sample; }
}
