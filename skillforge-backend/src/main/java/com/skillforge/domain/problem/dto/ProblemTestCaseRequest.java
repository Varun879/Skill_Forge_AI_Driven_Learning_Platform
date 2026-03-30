package com.skillforge.domain.problem.dto;

import jakarta.validation.constraints.NotBlank;

public class ProblemTestCaseRequest {

    @NotBlank(message = "Test case input is required")
    private String input;

    @NotBlank(message = "Test case expected output is required")
    private String output;

    private String explanation;

    private Boolean sample = true;

    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }

    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public Boolean getSample() { return sample; }
    public void setSample(Boolean sample) { this.sample = sample; }
}
