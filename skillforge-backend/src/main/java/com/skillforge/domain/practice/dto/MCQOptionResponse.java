package com.skillforge.domain.practice.dto;

import com.skillforge.domain.practice.entity.MCQOption;

public class MCQOptionResponse {

    private Long id;
    private String optionText;
    private Integer displayOrder;

    private MCQOptionResponse() {}

    public static MCQOptionResponse from(MCQOption option) {
        MCQOptionResponse response = new MCQOptionResponse();
        response.id = option.getId();
        response.optionText = option.getOptionText();
        response.displayOrder = option.getDisplayOrder();
        return response;
    }

    public Long getId() { return id; }
    public String getOptionText() { return optionText; }
    public Integer getDisplayOrder() { return displayOrder; }
}
