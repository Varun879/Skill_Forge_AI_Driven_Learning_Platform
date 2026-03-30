package com.skillforge.domain.ai.dto;

public class AIChatResponse {

    private String reply;
    private boolean hintOnly;
    private Long questionId;

    public AIChatResponse(String reply, boolean hintOnly, Long questionId) {
        this.reply = reply;
        this.hintOnly = hintOnly;
        this.questionId = questionId;
    }

    public String getReply() {
        return reply;
    }

    public boolean isHintOnly() {
        return hintOnly;
    }

    public Long getQuestionId() {
        return questionId;
    }
}
