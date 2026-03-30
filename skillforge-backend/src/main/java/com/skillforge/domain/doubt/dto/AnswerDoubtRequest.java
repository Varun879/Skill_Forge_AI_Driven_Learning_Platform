package com.skillforge.domain.doubt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AnswerDoubtRequest {

    @NotBlank(message = "Answer is required")
    @Size(max = 4000, message = "Answer must not exceed 4000 characters")
    private String answer;

    public String getAnswer() { return answer; }

    public void setAnswer(String answer) { this.answer = answer; }
}