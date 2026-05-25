package com.gym.repository;

import com.gym.entity.TeachingModality;
import com.gym.entity.VideoLesson;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoLessonRepository extends JpaRepository<VideoLesson, UUID> {

    @Override
    @EntityGraph(attributePaths = {"professor", "createdBy"})
    List<VideoLesson> findAll();

    @EntityGraph(attributePaths = {"professor", "createdBy"})
    List<VideoLesson> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"professor", "createdBy"})
    List<VideoLesson> findByCreatedByIdOrderByCreatedAtDesc(UUID createdById);

    @EntityGraph(attributePaths = {"professor", "createdBy"})
    Optional<VideoLesson> findById(UUID id);

    @EntityGraph(attributePaths = {"professor", "createdBy"})
    List<VideoLesson> findByActiveTrueAndMinimumPlanRankLessThanEqualOrderByCreatedAtDesc(Integer minimumPlanRank);

    @EntityGraph(attributePaths = {"professor", "createdBy"})
    List<VideoLesson> findByActiveTrueAndMinimumPlanRankLessThanEqualAndModalityInOrderByCreatedAtDesc(
        Integer minimumPlanRank,
        List<TeachingModality> modalities
    );
}
