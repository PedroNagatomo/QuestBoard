package com.questboard;

import com.questboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("devtools")
@RequiredArgsConstructor
public class DevToolsRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        String rawPassword = "admin123";
        String hash = passwordEncoder.encode(rawPassword);

        System.out.println();
        System.out.println("========================================================");
        System.out.println(">>> BCrypt de '" + rawPassword + "':");
        System.out.println(hash);
        System.out.println(">>> Match: " + passwordEncoder.matches(rawPassword, hash));
        System.out.println("========================================================");

        userRepository.findByUsername("admin").ifPresentOrElse(admin -> {
            admin.setPasswordHash(hash);
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println(">>> OK: Admin resetado com senha 'admin123'");
        }, () -> System.out.println(">>> ERRO: Admin nao encontrado"));

        System.out.println();
    }
}