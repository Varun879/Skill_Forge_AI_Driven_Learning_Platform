package com.skillforge.domain.submission.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillforge.domain.submission.entity.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByLearnerIdOrderByCreatedAtDesc(Long learnerId);

    List<Submission> findByProblemIdOrderByCreatedAtDesc(Long problemId);

    List<Submission> findByProblemTutorIdOrderByCreatedAtDesc(Long tutorId);
}
