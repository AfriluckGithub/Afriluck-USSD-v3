package org.gh.afriluck.afriluckussd.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gh.afriluck.afriluckussd.entities.Game;
import org.gh.afriluck.afriluckussd.repositories.GameRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TaskSchedulers implements Runnable {

    private final AfriluckCallHandler handler;
    private final GameRepository gameRepository;

    public TaskSchedulers(AfriluckCallHandler handler, GameRepository gameRepository) {
        this.handler = handler;
        this.gameRepository = gameRepository;
    }

    @Override
    @Scheduled(cron = "17 20 * * *", zone = "GMT")
    public void run() {
        try {
            System.out.println("Calling game options...");
            String game = handler.client().get().uri("/api/V1/game-info").retrieve().body(String.class);
            // System.out.println(game);
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
    }
}
