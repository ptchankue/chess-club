package za.co.tangentsolutions.chessclub.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.co.tangentsolutions.chessclub.models.Game;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    @Query("SELECT g FROM Game g JOIN FETCH g.player1 JOIN FETCH g.player2 WHERE g.player1.id = :player1Id OR g.player2.id = :player2Id ORDER BY g.playedAt DESC")
    List<Game> findByPlayer1IdOrPlayer2IdOrderByPlayedAtDesc(@Param("player1Id") Long player1Id, @Param("player2Id") Long player2Id);
    
    @Query("SELECT g FROM Game g JOIN FETCH g.player1 JOIN FETCH g.player2 WHERE g.player1.id = :playerId OR g.player2.id = :playerId ORDER BY g.playedAt DESC")
    List<Game> findMatchesByPlayerId(@Param("playerId") Long playerId);

    @Query("SELECT g FROM Game g JOIN FETCH g.player1 JOIN FETCH g.player2 ORDER BY g.playedAt DESC")
    List<Game> findAllByOrderByPlayedAtDesc();
}

