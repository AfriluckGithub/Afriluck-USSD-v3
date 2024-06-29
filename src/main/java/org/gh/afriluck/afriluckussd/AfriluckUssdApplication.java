package org.gh.afriluck.afriluckussd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gh.afriluck.afriluckussd.entities.Game;
import org.gh.afriluck.afriluckussd.repositories.GameRepository;
import org.gh.afriluck.afriluckussd.utils.AfriluckCallHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EnableScheduling
@EnableJpaRepositories(basePackages = "org.gh.afriluck.afriluckussd.repositories")
public class AfriluckUssdApplication {

    public AfriluckUssdApplication(AfriluckCallHandler handler, GameRepository gameRepository) {
        this.gameRepository = gameRepository;
        this.handler = handler;
    }

    public static void main(String[] args) {
        SpringApplication.run(AfriluckUssdApplication.class, args);
    }

    private final AfriluckCallHandler handler;
    private final GameRepository gameRepository;


    @Bean
    CommandLineRunner getGameOptions() {
        return args -> {
            try {
                System.out.println("Calling initial game options...");
                String game = handler.client().get().uri("/api/V1/game-info").retrieve().body(String.class);
                ObjectMapper mapper = new ObjectMapper();
                gameRepository.deleteAll();

                Game[] games = mapper.readValue(game, Game[].class);

                for (Game g : games) {
                    Game gts = new Game();
                    gts.setGameId(g.getGameId());
                    gts.setGameName(g.getGameName());
                    gts.setAmount(g.getAmount());
                    gts.setGameTypeId(g.getGameTypeId());
                    gts.setGameDraw(g.getGameDraw());
                    gts.setDrawTime(g.getDrawTime());
                    gts.setStartTime(g.getStartTime());
                    gts.setEndTime(g.getEndTime());
                    gameRepository.save(gts);
                }

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }

    ;

}
