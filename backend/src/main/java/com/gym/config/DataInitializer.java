package com.gym.config;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.gym.entity.Graduation;
import com.gym.entity.MartialArt;
import com.gym.entity.Plan;
import com.gym.entity.User;
import com.gym.repository.GraduationRepository;
import com.gym.repository.MartialArtRepository;
import com.gym.repository.PlanRepository;
import com.gym.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("!prod")
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final PasswordEncoder passwordEncoder;
    private final MartialArtRepository martialArtRepository;
    private final GraduationRepository graduationRepository;

    private static final String ADMIN_EMAIL = "admin@gym.com";
    private static final String ADMIN_PASSWORD = "Admin123!";

    @Bean
    CommandLineRunner initData() {
        return args -> {
            initAdminUser();
            initPlans();
            initMartialArts();
            initGraduations();
        };
    }

    private void initAdminUser() {
        List<User> existing = userRepository.findAll().stream()
                .filter(u -> ADMIN_EMAIL.equals(u.getEmail()))
                .toList();

        if (existing.isEmpty()) {
            User admin = User.builder()
                    .name("System Admin")
                    .email(ADMIN_EMAIL)
                    .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                    .role(User.Role.ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
            log.info("Admin ready: {}", ADMIN_EMAIL);
        } else if (existing.size() == 1) {
            User admin = existing.get(0);
            if (admin.getRole() != User.Role.ADMIN) {
                admin.setRole(User.Role.ADMIN);
                userRepository.save(admin);
                log.info("Admin ready: {}", ADMIN_EMAIL);
            } else {
                log.info("Admin ready: {}", ADMIN_EMAIL);
            }
        } else {
            log.warn("⚠️ Multiple admin users found ({}), keeping first one", existing.size());
        }
    }

    private void initPlans() {
        if (planRepository.count() > 0) {
            log.info("ℹ️ Plans already exist, skipping plan seeding.");
            return;
        }

        log.info("Seeding plans...");
        
        Plan basic = Plan.builder()
                .name("Basic")
                .description("Ideal for beginners who want to start their fitness journey with a solid foundation. This plan offers essential gym access with free weights, allowing you to establish a consistent workout routine. Perfect for those seeking quality at an affordable price.")
                .price(new BigDecimal("29.90"))
                .currency("EUR")
                .durationDays(30)
                .maxClasses(8)
                .features(List.of(
                    "Gym access",
                    "Free weights",
                    "Lockers",
                    "Basic workout app",
                    "Initial guidance support"
                ))
                .level("Beginner")
                .instructor("General Instructor")
                .schedule(List.of("Mon/Wed/Fri: 06h-10h", "Tue/Thu: 18h-22h"))
                .isActive(true)
                .build();

        Plan standard = Plan.builder()
                .name("Standard")
                .description("Perfect for regular practitioners who want variety in their workouts. In addition to full gym access, you have group classes (limited to 20 per month), monthly physical assessments to track your progress, and one monthly session with a personal trainer to optimize your results.")
                .price(new BigDecimal("49.90"))
                .currency("EUR")
                .durationDays(30)
                .maxClasses(20)
                .features(List.of(
                    "Gym access",
                    "Free weights",
                    "Group classes (up to 20/month)",
                    "Monthly physical assessment",
                    "Complete lockers",
                    "Premium workout app",
                    "1 monthly session with personal trainer",
                    "Access to rest area"
                ))
                .level("Intermediate")
                .instructor("Specialized Instructor")
                .schedule(List.of("Mon-Fri: 06h-22h", "Sat: 08h-14h"))
                .isActive(true)
                .build();

        Plan premium = Plan.builder()
                .name("Premium")
                .description("The ultimate experience for dedicated athletes. Enjoy 24-hour access, unlimited group classes, weekly personalized physical assessments, and exclusive benefits like included nutritionist, premium lockers with sauna, and invitations to special events. The most complete plan for those who demand excellence.")
                .price(new BigDecimal("79.90"))
                .currency("EUR")
                .durationDays(30)
                .maxClasses(100)
                .features(List.of(
                    "24h gym access",
                    "Free weights",
                    "Unlimited group classes",
                    "Weekly physical assessment",
                    "Kids waiting room",
                    "Coffee lounge area",
                    "2 monthly sessions with personal trainer",
                    "Nutritionist included",
                    "Premium lockers with sauna",
                    "Elite workout app",
                    "Invitations to exclusive events",
                    "Partner parking"
                ))
                .level("Advanced")
                .instructor("Jiu-Jitsu Master")
                .schedule(List.of("Mon-Sun: 24h", "Special classes on Saturdays"))
                .isActive(true)
                .build();

        planRepository.saveAll(List.of(basic, standard, premium));
        log.info("✅ Plans seeded successfully: Basic, Standard, Premium");
    }

    private void initMartialArts() {
        if (martialArtRepository.count() > 0) {
            log.info("ℹ️ Martial arts already exist, skipping martial art seeding.");
            return;
        }

        log.info("Seeding martial arts...");

        List<MartialArt> martialArts = List.of(
            MartialArt.builder().name("Jiu-Jitsu").build(),
            MartialArt.builder().name("Boxe / Kickboxing").build(),
            MartialArt.builder().name("Força & Condicionamento").build(),
            MartialArt.builder().name("Capoeira").build(),
            MartialArt.builder().name("MMA").build()
        );

        martialArtRepository.saveAll(martialArts);
        log.info("✅ Martial arts seeded: Jiu-Jitsu, Boxe / Kickboxing, Força & Condicionamento, Capoeira, MMA");
    }

    private void initGraduations() {
        if (graduationRepository.count() > 0) {
            log.info("ℹ️ Graduations already exist, skipping graduation seeding.");
            return;
        }

        log.info("Seeding graduations...");

        MartialArt jiuJitsu = martialArtRepository.findByName("Jiu-Jitsu").orElseThrow();
        MartialArt boxeKickboxing = martialArtRepository.findByName("Boxe / Kickboxing").orElseThrow();
        MartialArt forcaCondicionamento = martialArtRepository.findByName("Força & Condicionamento").orElseThrow();
        MartialArt capoeira = martialArtRepository.findByName("Capoeira").orElseThrow();
        MartialArt mma = martialArtRepository.findByName("MMA").orElseThrow();

        // Jiu-Jitsu belts
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

        // Boxe / Kickboxing levels
        List<Graduation> boxeKickboxingGraduations = List.of(
            Graduation.builder().name("Iniciante").levelOrder(1).martialArt(boxeKickboxing).build(),
            Graduation.builder().name("Intermédio").levelOrder(2).martialArt(boxeKickboxing).build(),
            Graduation.builder().name("Avançado").levelOrder(3).martialArt(boxeKickboxing).build(),
            Graduation.builder().name("Competição").levelOrder(4).martialArt(boxeKickboxing).build()
        );

        // Força & Condicionamento levels
        List<Graduation> forcaGraduations = List.of(
            Graduation.builder().name("Base").levelOrder(1).martialArt(forcaCondicionamento).build(),
            Graduation.builder().name("Intermédio").levelOrder(2).martialArt(forcaCondicionamento).build(),
            Graduation.builder().name("Avançado").levelOrder(3).martialArt(forcaCondicionamento).build(),
            Graduation.builder().name("Performance").levelOrder(4).martialArt(forcaCondicionamento).build()
        );

        // Capoeira belts
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

        // MMA levels
        List<Graduation> mmaGraduations = List.of(
            Graduation.builder().name("Fundamentos").levelOrder(1).martialArt(mma).build(),
            Graduation.builder().name("Intermédio").levelOrder(2).martialArt(mma).build(),
            Graduation.builder().name("Avançado").levelOrder(3).martialArt(mma).build(),
            Graduation.builder().name("Sparring").levelOrder(4).martialArt(mma).build(),
            Graduation.builder().name("Competição").levelOrder(5).martialArt(mma).build()
        );

        graduationRepository.saveAll(jiuJitsuGraduations);
        graduationRepository.saveAll(boxeKickboxingGraduations);
        graduationRepository.saveAll(forcaGraduations);
        graduationRepository.saveAll(capoeiraGraduations);
        graduationRepository.saveAll(mmaGraduations);

        log.info("✅ Graduations seeded for all martial arts");
    }
}
