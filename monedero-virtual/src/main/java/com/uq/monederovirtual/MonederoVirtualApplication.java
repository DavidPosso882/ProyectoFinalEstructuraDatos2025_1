package com.uq.monederovirtual;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan({ "com.uq.monederovirtual", "org.example" })
@EntityScan({ "com.uq.monederovirtual", "org.example" })
@EnableJpaRepositories({ "com.uq.monederovirtual", "org.example" })
@EnableScheduling
public class MonederoVirtualApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonederoVirtualApplication.class, args);
    }

}
