package com.gym.config;

import java.math.BigDecimal;
import java.util.List;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.gym.entity.Graduation;
import com.gym.entity.MartialArt;
import com.gym.entity.Plan;
import com.gym.repository.GraduationRepository;
import com.gym.repository.MartialArtRepository;
import com.gym.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("!prod")
@RequiredArgsConstructor
public class DataInitializer {

    private final PlanRepository planRepository;
    private final MartialArtRepository martialArtRepository;
    private final GraduationRepository graduationRepository;

    @PostConstruct
    public void init() {
        log.info("[STARTUP] ========== DataInitializer bean created (NOT prod profile) ==========");
    }

    @Bean
    CommandLineRunner initData() {
        return args -> {
            log.info("[STARTUP] ========== START CommandLineRunner initData ==========");
            initPlans();
            initMartialArts();
            initGraduations();
            log.info("[STARTUP] ========== END CommandLineRunner initData ==========");
        };
    }

    private void initPlans() {
        if (planRepository.count() > 0) {
            log.info("Plans already exist, skipping plan seeding.");
            return;
        }

        log.info("Seeding plans...");
        
        planRepository.saveAll(List.of(
                officialPlan("Sócio Fundador — Mensalidade 1 Modalidade — Adulto", "38.50", "Adulto", "1 modalidade", "Plano Sócio Fundador"),
                officialPlan("Sócio Fundador — Livre — Adulto", "63.00", "Adulto", "Modalidades livres", "Plano Sócio Fundador"),
                officialPlan("Sócio Fundador — Kids 1 Modalidade — Kids", "24.50", "Kids", "1 modalidade", "Plano Sócio Fundador"),
                officialPlan("Sócio Fundador — Kids 2 Modalidades — Kids", "38.50", "Kids", "2 modalidades", "Plano Sócio Fundador"),
                officialPlan("Mensalidade 1 Modalidade — Adulto", "55.00", "Adulto", "1 modalidade", "Plano Normal"),
                officialPlan("Mensalidade 2 Modalidades — Adulto", "75.00", "Adulto", "2 modalidades", "Plano Normal"),
                officialPlan("Livre — Adulto", "90.00", "Adulto", "Modalidades livres", "Plano Normal"),
                officialPlan("Kids 1 Modalidade — Kids", "35.00", "Kids", "1 modalidade", "Plano Normal"),
                officialPlan("Kids 2 Modalidades — Kids", "55.00", "Kids", "2 modalidades", "Plano Normal")));
        log.info("Official academy plans seeded successfully.");
    }

    private Plan officialPlan(String name, String price, String audience, String access, String group) {
        return Plan.builder()
                .name(name)
                .description(group + " para " + audience.toLowerCase() + " com " + access.toLowerCase() + ".")
                .price(new BigDecimal(price))
                .currency("EUR")
                .durationDays(30)
                .features(List.of(audience, access, group))
                .level(audience)
                .isActive(true)
                .build();
    }

    private void initMartialArts() {
        if (martialArtRepository.count() > 0) {
            log.info("Martial arts already exist, skipping martial art seeding.");
            return;
        }

        log.info("Seeding martial arts...");

        List<MartialArt> martialArts = List.of(
            MartialArt.builder().name("Jiu-Jitsu").build(),
            MartialArt.builder().name("Boxe / Kickboxing").build(),
            MartialArt.builder().name("Capoeira").build(),
            MartialArt.builder().name("MMA").build()
        );

        martialArtRepository.saveAll(martialArts);
        log.info("Martial arts seeded: Jiu-Jitsu, Boxe / Kickboxing, Capoeira, MMA");
    }

    private void initGraduations() {
        if (graduationRepository.count() > 0) {
            log.info("Graduations already exist, skipping graduation seeding.");
            return;
        }

        log.info("Seeding graduations...");

        MartialArt jiuJitsu = martialArtRepository.findByName("Jiu-Jitsu").orElseThrow();
        MartialArt boxeKickboxing = martialArtRepository.findByName("Boxe / Kickboxing").orElseThrow();
        MartialArt capoeira = martialArtRepository.findByName("Capoeira").orElseThrow();
        MartialArt mma = martialArtRepository.findByName("MMA").orElseThrow();

        List<Graduation> jiuJitsuGraduations = List.of(
            Graduation.builder().name("Branca").levelOrder(1).martialArt(jiuJitsu).build(),
            Graduation.builder().name("Cinzenta").levelOrder(2).martialArt(jiuJitsu).build(),
            Graduation.builder().name("Amarela").levelOrder(3).martialArt(jiuJitsu).build(),
            Graduation.builder().name("Laranja").levelOrder(4).martialArt(jiuJitsu).build(),
            Graduation.builder().name("Verde").levelOrder(5).martialArt(jiuJitsu).build(),
            Graduation.builder().name("Azul").levelOrder(6).martialArt(jiuJitsu).build(),
            Graduation.builder().name("Roxa").levelOrder(7).martialArt(jiuJitsu).build(),
            Graduation.builder().name("Castanha").levelOrder(8).martialArt(jiuJitsu).build(),
            Graduation.builder().name("Preta").levelOrder(9).martialArt(jiuJitsu).build()
        );

        List<Graduation> boxeKickboxingGraduations = List.of(
            Graduation.builder().name("Iniciante").levelOrder(1).martialArt(boxeKickboxing).build(),
            Graduation.builder().name("Intermedio").levelOrder(2).martialArt(boxeKickboxing).build(),
            Graduation.builder().name("Avancado").levelOrder(3).martialArt(boxeKickboxing).build(),
            Graduation.builder().name("Competicao").levelOrder(4).martialArt(boxeKickboxing).build()
        );

        List<Graduation> capoeiraGraduations = List.of(
            Graduation.builder().name("Crua").levelOrder(1).martialArt(capoeira).build(),
            Graduation.builder().name("Crua-Amarela").levelOrder(2).martialArt(capoeira).build(),
            Graduation.builder().name("Amarela").levelOrder(3).martialArt(capoeira).build(),
            Graduation.builder().name("Amarela-Laranja").levelOrder(4).martialArt(capoeira).build(),
            Graduation.builder().name("Laranja").levelOrder(5).martialArt(capoeira).build(),
            Graduation.builder().name("Laranja-Azul").levelOrder(6).martialArt(capoeira).build(),
            Graduation.builder().name("Azul").levelOrder(7).martialArt(capoeira).build(),
            Graduation.builder().name("Azul-Verde").levelOrder(8).martialArt(capoeira).build(),
            Graduation.builder().name("Verde").levelOrder(9).martialArt(capoeira).build(),
            Graduation.builder().name("Verde-Roxa").levelOrder(10).martialArt(capoeira).build(),
            Graduation.builder().name("Roxa").levelOrder(11).martialArt(capoeira).build(),
            Graduation.builder().name("Roxa-Marrom").levelOrder(12).martialArt(capoeira).build(),
            Graduation.builder().name("Marrom").levelOrder(13).martialArt(capoeira).build(),
            Graduation.builder().name("Marrom-Vermelha").levelOrder(14).martialArt(capoeira).build(),
            Graduation.builder().name("Vermelha").levelOrder(15).martialArt(capoeira).build()
        );

        List<Graduation> mmaGraduations = List.of(
            Graduation.builder().name("Fundamentos").levelOrder(1).martialArt(mma).build(),
            Graduation.builder().name("Intermedio").levelOrder(2).martialArt(mma).build(),
            Graduation.builder().name("Avancado").levelOrder(3).martialArt(mma).build(),
            Graduation.builder().name("Sparring").levelOrder(4).martialArt(mma).build(),
            Graduation.builder().name("Competicao").levelOrder(5).martialArt(mma).build()
        );

        graduationRepository.saveAll(jiuJitsuGraduations);
        graduationRepository.saveAll(boxeKickboxingGraduations);
        graduationRepository.saveAll(capoeiraGraduations);
        graduationRepository.saveAll(mmaGraduations);

        log.info("Graduations seeded for all martial arts");
    }
}
