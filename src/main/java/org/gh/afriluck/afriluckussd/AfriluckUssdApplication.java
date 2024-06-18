package org.gh.afriluck.afriluckussd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EnableScheduling
@EnableJpaRepositories(basePackages = "org.gh.afriluck.afriluckussd.repositories")
public class AfriluckUssdApplication {

    public static void main(String[] args) {
        SpringApplication.run(AfriluckUssdApplication.class, args);
    }

}
