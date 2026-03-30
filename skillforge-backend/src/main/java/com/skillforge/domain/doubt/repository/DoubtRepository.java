package com.skillforge.domain.doubt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillforge.common.enums.DoubtStatus;
import com.skillforge.domain.doubt.entity.Doubt;

@Repository
public interface DoubtRepository extends JpaRepository<Doubt, Long> {

    List<Doubt> findByLearnerIdOrderByCreatedAtDesc(Long learnerId);

    List<Doubt> findByLearnerIdAndProblemIdOrderByCreatedAtDesc(Long learnerId, Long problemId);

    List<Doubt> findByLearnerIdAndStatusOrderByCreatedAtDesc(Long learnerId, DoubtStatus status);

    List<Doubt> findByLearnerIdAndProblemIdAndStatusOrderByCreatedAtDesc(Long learnerId, Long problemId, DoubtStatus status);

    List<Doubt> findByProblemTutorIdOrderByCreatedAtDesc(Long tutorId);

    List<Doubt> findByProblemTutorIdAndProblemIdOrderByCreatedAtDesc(Long tutorId, Long problemId);

    List<Doubt> findByProblemTutorIdAndStatusOrderByCreatedAtDesc(Long tutorId, DoubtStatus status);

    List<Doubt> findByProblemTutorIdAndProblemIdAndStatusOrderByCreatedAtDesc(Long tutorId, Long problemId, DoubtStatus status);
}