package com.skillforge.domain.practice.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "mcq_options")
public class MCQOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private PracticeQuestion question;

    @Column(name = "option_text", nullable = false, length = 500)
    private String optionText;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    @OneToMany(mappedBy = "selectedOption", fetch = FetchType.LAZY)
    private List<UserAnswer> userAnswers = new ArrayList<>();

    public MCQOption() {}

    public Long getId() { return id; }
    public PracticeQuestion getQuestion() { return question; }
    public String getOptionText() { return optionText; }
    public Integer getDisplayOrder() { return displayOrder; }
    public Boolean getIsCorrect() { return isCorrect; }

    public void setQuestion(PracticeQuestion question) { this.question = question; }
    public void setOptionText(String optionText) { this.optionText = optionText; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public void setIsCorrect(Boolean correct) { isCorrect = correct; }
}
