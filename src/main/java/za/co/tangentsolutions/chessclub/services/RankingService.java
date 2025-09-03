package za.co.tangentsolutions.chessclub.services;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.tangentsolutions.chessclub.models.Game;
import za.co.tangentsolutions.chessclub.models.Member;
import za.co.tangentsolutions.chessclub.repositories.GameRepository;
import za.co.tangentsolutions.chessclub.repositories.MemberRepository;

import java.util.List;

@Service
public class RankingService {
    
    private final MemberRepository memberRepository;
    private final GameRepository gameRepository;

    private static final Logger logger = LogManager.getLogger(RankingService.class);

    @Autowired
    public RankingService(MemberRepository memberRepository, GameRepository gameRepository) {
        this.memberRepository = memberRepository;
        this.gameRepository = gameRepository;
    }

    @Transactional
    public Game recordMatch(Long player1Id, Long player2Id, int player1Score, int player2Score) {
        Member player1 = memberRepository.findById(player1Id)
            .orElseThrow(() -> new RuntimeException("Player 1 not found"));
        Member player2 = memberRepository.findById(player2Id)
            .orElseThrow(() -> new RuntimeException("Player 2 not found"));
        
        if (player1.equals(player2)) {
            throw new RuntimeException("Cannot play against yourself");
        }
        
        // Create game record
        Game game = new Game(player1, player2, player1Score, player2Score);
        
        // Process ranking changes
        processRankingChanges(game);
        
        // Update games played count
        player1.setGamesPlayed(player1.getGamesPlayed() + 1);
        player2.setGamesPlayed(player2.getGamesPlayed() + 1);
        
        // Save updated members and game
        memberRepository.save(player1);
        memberRepository.save(player2);
        
        game.setPlayer1RankAfter(player1.getRank());
        game.setPlayer2RankAfter(player2.getRank());
        
        return gameRepository.save(game);
    }

    public List<Game> allMatches(){
        return gameRepository.findAll();
    }
    private void processRankingChanges(Game game) {
        logger.info("Processing ranking for game: {}", game);
        
        Member player1 = game.getPlayer1();
        Member player2 = game.getPlayer2();
        
        logger.info("Player1: {} (rank {}), Player2: {} (rank {})", 
            player1.getFullName(), player1.getRank(), 
            player2.getFullName(), player2.getRank());
        
        // Determine higher and lower ranked players
        Member higherRanked, lowerRanked;
        if (player1.getRank() < player2.getRank()) {
            higherRanked = player1;
            lowerRanked = player2;
        } else {
            higherRanked = player2;
            lowerRanked = player1;
        }
        
        logger.info("Higher ranked: {} (rank {}), Lower ranked: {} (rank {})", 
            higherRanked.getFullName(), higherRanked.getRank(), 
            lowerRanked.getFullName(), lowerRanked.getRank());
        
        if (game.isDraw()) {
            logger.info("Game is a draw");
            handleDraw(higherRanked, lowerRanked);
        } else {
            Member winner = game.getWinner();
            Member loser = game.getLoser();
            
            logger.info("Winner: {} (rank {}), Loser: {} (rank {})", 
                winner.getFullName(), winner.getRank(), 
                loser.getFullName(), loser.getRank());
            
            if (winner.equals(lowerRanked)) {
                logger.info("Lower ranked player won - processing upset");
                handleLowerRankedWin(higherRanked, lowerRanked);
            } else {
                logger.info("Higher ranked player won - no ranking change");
            }
        }
    }
    
    private void handleDraw(Member higherRanked, Member lowerRanked) {
        int rankDifference = lowerRanked.getRank() - higherRanked.getRank();
        if (rankDifference > 1) {
            // Lower ranked player moves up one position
            int newRank = lowerRanked.getRank() - 1;
            memberRepository.incrementRanks(newRank, lowerRanked.getRank());
            lowerRanked.setRank(newRank);
        }
    }
    
    private void handleLowerRankedWin(Member higherRanked, Member lowerRanked) {
        int rankDifference = lowerRanked.getRank() - higherRanked.getRank();
        
        logger.info("Processing lower ranked win: {} (rank {}) beats {} (rank {}), difference={}", 
            lowerRanked.getFullName(), lowerRanked.getRank(), 
            higherRanked.getFullName(), higherRanked.getRank(), rankDifference);
        
        // For ANY upset, the higher ranked player should move down
        if (rankDifference >= 1) {
            // Higher ranked moves down one position
            int newHigherRank = higherRanked.getRank() + 1;
            logger.info("Upset: {} moves down from rank {} to {}", 
                higherRanked.getFullName(), higherRanked.getRank(), newHigherRank);
            memberRepository.decrementRanks(higherRanked.getRank(), newHigherRank);
            higherRanked.setRank(newHigherRank);
            
            // Lower ranked moves up by at least 1 position, or more for larger differences
            int moveUp = Math.max(1, rankDifference / 2);
            int newLowerRank = lowerRanked.getRank() - moveUp;
            logger.info("Upset: {} moves up from rank {} to {}", 
                lowerRanked.getFullName(), lowerRanked.getRank(), newLowerRank);
            memberRepository.incrementRanks(newLowerRank, lowerRanked.getRank());
            lowerRanked.setRank(newLowerRank);
        } else {
            logger.info("Upset with no rank difference: no change");
        }
    }
    
    public List<Game> getGameHistory() {
        return gameRepository.findAllByOrderByPlayedAtDesc();
    }
    
    public List<Game> getPlayerGameHistory(Long playerId) {
        return gameRepository.findMatchesByPlayerId(playerId);
    }
}