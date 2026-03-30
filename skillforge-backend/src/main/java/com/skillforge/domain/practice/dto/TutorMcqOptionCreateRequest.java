package com.skillforge.domain.practice.dto;

import jakarta.validation.constraints.NotBlank;

public class TutorMcqOptionCreateRequest {

    @NotBlank(message = "optionText is required")
    private String optionText;

    public TutorMcqOptionCreateRequest() {
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }
}
