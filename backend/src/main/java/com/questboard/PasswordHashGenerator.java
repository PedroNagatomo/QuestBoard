package com.questboard;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("hashgen")
public class PasswordHashGenerator implements CommandLineRunner {
    @Override
    public void run(String... args) {
        var enc = new BCryptPasswordEncoder(10);
        String hash = enc.encode("admin123");
        System.out.println(">>> BCrypt de 'admin123' = " + hash);
        // valida ida e volta
        System.out.println(">>> Match: " + enc.matches("admin123", hash));
        System.exit(0);
    }
}