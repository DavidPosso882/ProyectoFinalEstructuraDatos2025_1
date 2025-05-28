package org.example.config;

import org.example.model.Role;
import org.example.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleInitializer.class);
    
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Inicializar roles si no existen
        if (roleRepository.count() == 0) {
            logger.info("Inicializando roles...");
            
            Role userRole = new Role(Role.ERole.ROLE_USER);
            Role adminRole = new Role(Role.ERole.ROLE_ADMIN);
            
            roleRepository.save(userRole);
            roleRepository.save(adminRole);
            
            logger.info("Roles inicializados correctamente");
        }
    }
}
