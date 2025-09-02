package za.co.tangentsolutions.chessclub.services;


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
        Member player1 = game.getPlayer1();
        Member player2 = game.getPlayer2();
        
        // Determine higher and lower ranked players
        Member higherRanked, lowerRanked;
        if (player1.getRank() < player2.getRank()) {
            higherRanked = player1;
            lowerRanked = player2;
        } else {
            higherRanked = player2;
            lowerRanked = player1;
        }
        
        if (game.isDraw()) {
            handleDraw(higherRanked, lowerRanked);
        } else {
            Member winner = game.getWinner();
            Member loser = game.getLoser();
            
            if (winner.equals(lowerRanked)) {
                handleLowerRankedWin(higherRanked, lowerRanked);
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
        int moveUp = rankDifference / 2;
        
        if (moveUp > 0) {
            // Higher ranked moves down one position
            int newHigherRank = higherRanked.getRank() + 1;
            memberRepository.decrementRanks(higherRanked.getRank(), newHigherRank);
            higherRanked.setRank(newHigherRank);
            
            // Lower ranked moves up by half the difference
            int newLowerRank = lowerRanked.getRank() - moveUp;
            memberRepository.incrementRanks(newLowerRank, lowerRanked.getRank());
            lowerRanked.setRank(newLowerRank);
        }
    }
    
    public List<Game> getGameHistory() {
        return gameRepository.findAllByOrderByPlayedAtDesc();
    }
    
    public List<Game> getPlayerGameHistory(Long playerId) {
        return gameRepository.findMatchesByPlayerId(playerId);
    }
}