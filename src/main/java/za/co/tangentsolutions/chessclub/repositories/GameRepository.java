package za.co.tangentsolutions.chessclub.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.co.tangentsolutions.chessclub.models.Game;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByPlayer1IdOrPlayer2IdOrderByPlayedAtDesc(Long player1Id, Long player2Id);
    
    @Query("SELECT g FROM Game g WHERE g.player1.id = :playerId OR g.player2.id = :playerId ORDER BY g.playedAt DESC")
    List<Game> findMatchesByPlayerId(@Param("playerId") Long playerId);

    List<Game> findAllByOrderByPlayedAtDesc();
}

