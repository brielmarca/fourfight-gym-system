package com.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gym.entity.Plan;
import com.gym.repository.PlanRepository;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private PlanService service;

    @Test
    void getAllActiveExcludesInactiveFounderPlan() {
        UUID founderPlanId = UUID.fromString("4f280001-0000-4000-8000-000000000001");
        UUID normalPlanId = UUID.fromString("4f280001-0000-4000-8000-000000000005");
        Plan founderPlan = Plan.builder()
                .id(founderPlanId)
                .name("Sócio Fundador — Mensalidade 1 Modalidade — Adulto")
                .isActive(false)
                .build();
        Plan normalPlan = Plan.builder()
                .id(normalPlanId)
                .name("Mensalidade 1 Modalidade — Adulto")
                .isActive(true)
                .build();

        when(planRepository.findAll()).thenReturn(List.of(founderPlan, normalPlan));

        assertThat(service.getAllActive())
                .extracting(response -> response.id())
                .containsExactly(normalPlanId);
    }
}
