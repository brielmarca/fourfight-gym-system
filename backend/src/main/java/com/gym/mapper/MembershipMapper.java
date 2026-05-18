package com.gym.mapper;

import com.gym.dto.request.CreateMembershipRequest;
import com.gym.dto.response.MembershipResponse;
import com.gym.entity.Membership;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MembershipMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    Membership toEntity(CreateMembershipRequest request);

    default MembershipResponse toResponse(Membership membership) {
        return MembershipResponse.from(membership);
    }

    List<MembershipResponse> toResponseList(List<Membership> memberships);
}