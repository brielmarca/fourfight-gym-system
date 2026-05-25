package com.gym.service;

import com.gym.dto.request.CreateVideoLessonRequest;
import com.gym.dto.request.UpdateVideoLessonRequest;
import com.gym.dto.response.VideoLessonResponse;
import com.gym.entity.Membership;
import com.gym.entity.ProfessorModality;
import com.gym.entity.TeachingModality;
import com.gym.entity.User;
import com.gym.entity.VideoLesson;
import com.gym.exception.BusinessRuleException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.exception.UnauthorizedException;
import com.gym.repository.MembershipRepository;
import com.gym.repository.ProfessorModalityRepository;
import com.gym.repository.UserRepository;
import com.gym.repository.VideoLessonRepository;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VideoLessonService {

    private final VideoLessonRepository videoLessonRepository;
    private final UserRepository userRepository;
    private final ProfessorModalityRepository professorModalityRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public VideoLessonResponse create(CreateVideoLessonRequest request, UUID actorId, String actorRole) {
        User actor = getUser(actorId);
        VideoMetadata metadata = parseAndValidateVideoUrl(request.videoUrl());
        validatePlanRank(request.minimumPlanRank());

        VideoLesson lesson = VideoLesson.builder()
            .title(request.title().trim())
            .description(request.description() != null ? request.description().trim() : null)
            .modality(request.modality())
            .videoUrl(metadata.videoUrl())
            .embedUrl(metadata.embedUrl())
            .provider(metadata.provider())
            .minimumPlanRank(request.minimumPlanRank())
            .active(request.active() == null || request.active())
            .createdBy(actor)
            .build();

        if (isAdminOrManager(actorRole)) {
            lesson.setProfessor(resolveProfessorOptional(request.professorId()));
        } else if (isProfessor(actorRole)) {
            validateProfessorCanManageModality(actor.getId(), request.modality());
            lesson.setProfessor(actor);
        } else {
            throw new UnauthorizedException("Role cannot create video lessons");
        }

        return VideoLessonResponse.from(videoLessonRepository.save(lesson));
    }

    @Transactional(readOnly = true)
    public List<VideoLessonResponse> listManage(UUID actorId, String actorRole) {
        if (isAdminOrManager(actorRole)) {
            return videoLessonRepository.findAllByOrderByCreatedAtDesc().stream().map(VideoLessonResponse::from).toList();
        }
        if (isProfessor(actorRole)) {
            return videoLessonRepository.findByCreatedByIdOrderByCreatedAtDesc(actorId).stream().map(VideoLessonResponse::from).toList();
        }
        throw new UnauthorizedException("Role cannot manage video lessons");
    }

    @Transactional
    public VideoLessonResponse update(UUID lessonId, UpdateVideoLessonRequest request, UUID actorId, String actorRole) {
        VideoLesson lesson = getLesson(lessonId);
        assertCanManageLesson(lesson, actorId, actorRole);
        VideoMetadata metadata = parseAndValidateVideoUrl(request.videoUrl());
        validatePlanRank(request.minimumPlanRank());

        lesson.setTitle(request.title().trim());
        lesson.setDescription(request.description() != null ? request.description().trim() : null);
        lesson.setModality(request.modality());
        lesson.setVideoUrl(metadata.videoUrl());
        lesson.setEmbedUrl(metadata.embedUrl());
        lesson.setProvider(metadata.provider());
        lesson.setMinimumPlanRank(request.minimumPlanRank());
        lesson.setActive(request.active() == null || request.active());

        if (isAdminOrManager(actorRole)) {
            lesson.setProfessor(resolveProfessorOptional(request.professorId()));
        } else {
            validateProfessorCanManageModality(actorId, request.modality());
            lesson.setProfessor(getUser(actorId));
        }

        return VideoLessonResponse.from(videoLessonRepository.save(lesson));
    }

    @Transactional
    public VideoLessonResponse deactivate(UUID lessonId, UUID actorId, String actorRole) {
        VideoLesson lesson = getLesson(lessonId);
        assertCanManageLesson(lesson, actorId, actorRole);
        lesson.setActive(false);
        return VideoLessonResponse.from(videoLessonRepository.save(lesson));
    }

    @Transactional(readOnly = true)
    public List<VideoLessonResponse> listMyLessons(UUID actorId, String actorRole) {
        if (isAdminOrManager(actorRole)) {
            return videoLessonRepository.findAllByOrderByCreatedAtDesc().stream().map(VideoLessonResponse::from).toList();
        }

        if (isProfessor(actorRole)) {
            return videoLessonRepository.findByCreatedByIdOrderByCreatedAtDesc(actorId).stream().map(VideoLessonResponse::from).toList();
        }

        Membership membership = membershipRepository
            .findTopByUserIdAndStatusOrderByCreatedAtDesc(actorId, Membership.MembershipStatus.ACTIVE)
            .orElseThrow(() -> new UnauthorizedException("No active membership found"));

        int memberRank = mapPlanRank(membership.getPlan().getName());
        List<VideoLesson> lessons = videoLessonRepository.findByActiveTrueAndMinimumPlanRankLessThanEqualOrderByCreatedAtDesc(memberRank);
        return lessons.stream().map(VideoLessonResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public VideoLessonResponse getMyLessonById(UUID lessonId, UUID actorId, String actorRole) {
        VideoLesson lesson = getLesson(lessonId);

        if (isAdminOrManager(actorRole)) {
            return VideoLessonResponse.from(lesson);
        }

        if (isProfessor(actorRole)) {
            assertCanManageLesson(lesson, actorId, actorRole);
            return VideoLessonResponse.from(lesson);
        }

        if (!Boolean.TRUE.equals(lesson.getActive())) {
            throw new ResourceNotFoundException("VideoLesson", lessonId);
        }

        Membership membership = membershipRepository
            .findTopByUserIdAndStatusOrderByCreatedAtDesc(actorId, Membership.MembershipStatus.ACTIVE)
            .orElseThrow(() -> new UnauthorizedException("No active membership found"));
        int memberRank = mapPlanRank(membership.getPlan().getName());
        if (memberRank < lesson.getMinimumPlanRank()) {
            throw new UnauthorizedException("You do not have access to this lesson");
        }
        return VideoLessonResponse.from(lesson);
    }

    private void assertCanManageLesson(VideoLesson lesson, UUID actorId, String actorRole) {
        if (isAdminOrManager(actorRole)) {
            return;
        }
        if (isProfessor(actorRole) && lesson.getCreatedBy().getId().equals(actorId)) {
            return;
        }
        throw new UnauthorizedException("You cannot manage this lesson");
    }

    private void validateProfessorCanManageModality(UUID professorId, TeachingModality modality) {
        List<ProfessorModality> modalities = professorModalityRepository.findByProfessorId(professorId);
        if (modalities.isEmpty()) {
            throw new BusinessRuleException("Professor has no configured modalities");
        }
        boolean canTeach = modalities.stream().anyMatch(item -> item.getModality() == modality);
        if (!canTeach) {
            throw new UnauthorizedException("Professor is not assigned to this modality");
        }
    }

    private User resolveProfessorOptional(UUID professorId) {
        if (professorId == null) {
            return null;
        }
        User professor = getUser(professorId);
        if (professor.getRole() != User.Role.PROFESSOR) {
            throw new BusinessRuleException("Provided professorId does not belong to a professor");
        }
        return professor;
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private VideoLesson getLesson(UUID lessonId) {
        return videoLessonRepository.findById(lessonId)
            .orElseThrow(() -> new ResourceNotFoundException("VideoLesson", lessonId));
    }

    private boolean isAdminOrManager(String role) {
        return "ADMIN".equals(role) || "MANAGER".equals(role);
    }

    private boolean isProfessor(String role) {
        return "PROFESSOR".equals(role);
    }

    private void validatePlanRank(Integer rank) {
        if (rank == null || rank < 1 || rank > 3) {
            throw new BusinessRuleException("minimumPlanRank must be between 1 and 3");
        }
    }

    private int mapPlanRank(String planName) {
        String normalized = planName == null ? "" : planName.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "BASIC" -> 1;
            case "STANDARD" -> 2;
            case "PREMIUM" -> 3;
            default -> 1;
        };
    }

    private VideoMetadata parseAndValidateVideoUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new BusinessRuleException("videoUrl is required");
        }

        URI uri;
        try {
            uri = URI.create(rawUrl.trim());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("Invalid video URL");
        }

        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new BusinessRuleException("Only http/https video URLs are accepted");
        }

        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        if (host.startsWith("www.")) {
            host = host.substring(4);
        }

        if ("youtube.com".equals(host)) {
            String videoId = extractYoutubeVideoId(uri);
            return new VideoMetadata(rawUrl.trim(), "https://www.youtube.com/embed/" + videoId, VideoLesson.VideoProvider.YOUTUBE);
        }
        if ("youtu.be".equals(host)) {
            String path = uri.getPath() == null ? "" : uri.getPath();
            String videoId = path.startsWith("/") ? path.substring(1) : path;
            if (videoId.isBlank()) {
                throw new BusinessRuleException("Invalid YouTube URL");
            }
            return new VideoMetadata(rawUrl.trim(), "https://www.youtube.com/embed/" + videoId, VideoLesson.VideoProvider.YOUTUBE);
        }
        if ("vimeo.com".equals(host)) {
            String path = uri.getPath() == null ? "" : uri.getPath();
            String id = path.startsWith("/") ? path.substring(1) : path;
            if (!id.matches("\\d+")) {
                throw new BusinessRuleException("Invalid Vimeo URL");
            }
            return new VideoMetadata(rawUrl.trim(), "https://player.vimeo.com/video/" + id, VideoLesson.VideoProvider.VIMEO);
        }

        throw new BusinessRuleException("Only YouTube and Vimeo URLs are accepted");
    }

    private String extractYoutubeVideoId(URI uri) {
        String path = uri.getPath() == null ? "" : uri.getPath();
        if (path.startsWith("/embed/")) {
            String id = path.substring("/embed/".length());
            if (!id.isBlank()) {
                return id;
            }
        }
        if (path.startsWith("/shorts/")) {
            String id = path.substring("/shorts/".length());
            if (!id.isBlank()) {
                return id;
            }
        }

        String query = uri.getQuery();
        if (query != null) {
            for (String pair : query.split("&")) {
                String[] parts = pair.split("=", 2);
                if (parts.length == 2 && "v".equals(parts[0]) && !parts[1].isBlank()) {
                    return parts[1];
                }
            }
        }
        throw new BusinessRuleException("Invalid YouTube URL");
    }

    private record VideoMetadata(String videoUrl, String embedUrl, VideoLesson.VideoProvider provider) {}
}
