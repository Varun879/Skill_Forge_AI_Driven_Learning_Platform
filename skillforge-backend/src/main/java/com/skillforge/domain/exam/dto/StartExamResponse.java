package com.skillforge.domain.exam.dto;

import java.time.LocalDateTime;
import java.util.List;

public class StartExamResponse {

    private Long examSessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationSeconds;
    private List<ExamQuestionItem> questions;

    public StartExamResponse(Long examSessionId,
                             LocalDateTime startTime,
                             LocalDateTime endTime,
                             Integer durationSeconds,
                             List<ExamQuestionItem> questions) {
        this.examSessionId = examSessionId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationSeconds = durationSeconds;
        this.questions = questions;
    }

    public Long getExamSessionId() {
        return examSessionId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public List<ExamQuestionItem> getQuestions() {
        return questions;
    }

    public static class ExamQuestionItem {
        private Long questionId;
        private String title;
        private String prompt;
        private String topic;
        private String difficulty;
        private int order;
        private List<OptionItem> options;

        public ExamQuestionItem(Long questionId,
                                String title,
                                String prompt,
                                String topic,
                                String difficulty,
                                int order,
                                List<OptionItem> options) {
            this.questionId = questionId;
            this.title = title;
            this.prompt = prompt;
            this.topic = topic;
            this.difficulty = difficulty;
            this.order = order;
            this.options = options;
        }

        public Long getQuestionId() {
            return questionId;
        }

        public String getTitle() {
            return title;
        }

        public String getPrompt() {
            return prompt;
        }

        public String getTopic() {
            return topic;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public int getOrder() {
            return order;
        }

        public List<OptionItem> getOptions() {
            return options;
        }
    }

    public static class OptionItem {
        private Long optionId;
        private String text;

        public OptionItem(Long optionId, String text) {
            this.optionId = optionId;
            this.text = text;
        }

        public Long getOptionId() {
            return optionId;
        }

        public String getText() {
            return text;
        }
    }
}
