package za.co.tangentsolutions.chessclub.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.tangentsolutions.chessclub.models.Match;
import za.co.tangentsolutions.chessclub.models.Member;
import za.co.tangentsolutions.chessclub.repositories.MatchRepository;
import za.co.tangentsolutions.chessclub.repositories.MemberRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RankingService {
    
    private final MemberRepository memberRepository;
    

    private final MatchRepository matchRepository;

    @Autowired
    public RankingService(MemberRepository memberRepository, MatchRepository matchRepository) {
        this.memberRepository = memberRepository;
        this.matchRepository = matchRepository;
    }

    @Transactional
    public Match recordMatch(Long player1Id, Long player2Id, int player1Score, int player2Score) {
        Member player1 = memberRepository.findById(player1Id)
            .orElseThrow(() -> new RuntimeException("Player 1 not found"));
        Member player2 = memberRepository.findById(player2Id)
            .orElseThrow(() -> new RuntimeException("Player 2 not found"));
        
        if (player1.equals(player2)) {
            throw new RuntimeException("Cannot play against yourself");
        }
        
        // Create game record
        Match match = new Match(player1, player2, player1Score, player2Score);
        
        // Process ranking changes
        processRankingChanges(match);
        
        // Update games played count
        player1.setGamesPlayed(player1.getGamesPlayed() + 1);
        player2.setGamesPlayed(player2.getGamesPlayed() + 1);
        
        // Save updated members and game
        memberRepository.save(player1);
        memberRepository.save(player2);
        
        match.setPlayer1RankAfter(player1.getRank());
        match.setPlayer2RankAfter(player2.getRank());
        
        return matchRepository.save(match);
    }

    public List<Match> allMatches(){
        return matchRepository.findAll();
    }
    private void processRankingChanges(Match match) {
        Member player1 = match.getPlayer1();
        Member player2 = match.getPlayer2();
        
        // Determine higher and lower ranked players
        Member higherRanked, lowerRanked;
        if (player1.getRank() < player2.getRank()) {
            higherRanked = player1;
            lowerRanked = player2;
        } else {
            higherRanked = player2;
            lowerRanked = player1;
        }
        
        if (match.isDraw()) {
            handleDraw(higherRanked, lowerRanked);
        } else {
            Member winner = match.getWinner();
            Member loser = match.getLoser();
            
            if (winner.equals(lowerRanked)) {
                handleLowerRankedWin(higherRanked, lowerRanked);
            }
            // Higher ranked wins - no changes needed
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
    
    public List<Match> getGameHistory() {
        return matchRepository.findAllByOrderByPlayedAtDesc();
    }
    
    public List<Match> getPlayerGameHistory(Long playerId) {
        return matchRepository.findMatchesByPlayerId(playerId);
    }
}