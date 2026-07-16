package com.gym.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.gym.entity.Membership;
import com.gym.entity.Plan;
import com.gym.entity.User;

class AdminStudentResponseTest {

    @Test
    void includesPriceFromInactiveMembershipPlan() {
        Plan inactivePlan = Plan.builder()
                .name("Basic")
                .price(new BigDecimal("29.90"))
                .isActive(false)
                .build();
        Membership membership = Membership.builder()
                .plan(inactivePlan)
                .status(Membership.MembershipStatus.ACTIVE)
                .build();

        AdminStudentResponse response = AdminStudentResponse.from(User.builder().build(), membership);

        assertThat(response.planPrice()).isEqualByComparingTo("29.90");
    }
}
