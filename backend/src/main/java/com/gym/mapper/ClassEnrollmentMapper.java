package com.gym.mapper;

import com.gym.dto.response.ClassEnrollmentResponse;
import com.gym.entity.ClassEnrollment;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClassEnrollmentMapper {

    default ClassEnrollmentResponse toResponse(ClassEnrollment enrollment) {
        return ClassEnrollmentResponse.from(enrollment);
    }

    List<ClassEnrollmentResponse> toResponseList(List<ClassEnrollment> enrollments);
}