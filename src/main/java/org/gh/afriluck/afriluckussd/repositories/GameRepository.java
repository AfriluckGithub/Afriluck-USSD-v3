package org.gh.afriluck.afriluckussd.repositories;

import org.gh.afriluck.afriluckussd.entities.Game;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends CrudRepository<Game, Integer> {

    List<Game> findAll();

}
