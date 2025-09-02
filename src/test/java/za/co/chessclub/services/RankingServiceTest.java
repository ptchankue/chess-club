package za.co.chessclub.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.tangentsolutions.chessclub.models.Game;
import za.co.tangentsolutions.chessclub.models.Member;
import za.co.tangentsolutions.chessclub.repositories.GameRepository;
import za.co.tangentsolutions.chessclub.repositories.MemberRepository;
import za.co.tangentsolutions.chessclub.services.RankingService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private GameRepository gameRepository;
    
    @InjectMocks
    private RankingService rankingService;
    
    private Member higherRanked;
    private Member lowerRanked;
    private Game testGame;
    
    @BeforeEach
    void setUp() {
        higherRanked = new Member("John", "Doe", "john@email.com", 
                                LocalDate.of(1985, 5, 15), 3);
        higherRanked.setId(1L);
        higherRanked.setGamesPlayed(10);
        
        lowerRanked = new Member("Jane", "Smith", "jane@email.com", 
                               LocalDate.of(1990, 8, 22), 8);
        lowerRanked.setId(2L);
        lowerRanked.setGamesPlayed(8);
        
        testGame = new Game(higherRanked, lowerRanked, 1, 0);
    }
    
    @Test
    void recordMatch_WhenHigherRankedWins_ShouldNotChangeRanks() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(higherRanked));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(lowerRanked));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);
        
        // Act
        Game result = rankingService.recordMatch(1L, 2L, 1, 0);
        
        // Assert
        assertEquals(3, higherRanked.getRank()); // No change
        assertEquals(8, lowerRanked.getRank());  // No change
        assertEquals(11, higherRanked.getGamesPlayed());
        assertEquals(9, lowerRanked.getGamesPlayed());
        verify(memberRepository, times(2)).save(any(Member.class));
        verify(gameRepository).save(any(Game.class));
    }
    
    @Test
    void recordMatch_WhenDrawWithNonAdjacentRanks_ShouldMoveLowerRankedUp() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(higherRanked));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(lowerRanked));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);
        doNothing().when(memberRepository).incrementRanks(7, 8);
        
        // Act
        Game result = rankingService.recordMatch(1L, 2L, 0, 0);
        
        // Assert
        assertEquals(7, lowerRanked.getRank()); // Moved up one position
        assertEquals(3, higherRanked.getRank()); // No change
        verify(memberRepository).incrementRanks(7, 8);
        verify(memberRepository, times(2)).save(any(Member.class));
    }
    
    @Test
    void recordMatch_WhenDrawWithAdjacentRanks_ShouldNotChangeRanks() {
        // Arrange
        Member adjacentLower = new Member("Adjacent", "Player", "adjacent@email.com", 
                                        LocalDate.of(1990, 8, 22), 4);
        adjacentLower.setId(3L);
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(higherRanked));
        when(memberRepository.findById(3L)).thenReturn(Optional.of(adjacentLower));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);
        
        // Act
        Game result = rankingService.recordMatch(1L, 3L, 0, 0);
        
        // Assert
        assertEquals(3, higherRanked.getRank()); // No change
        assertEquals(4, adjacentLower.getRank()); // No change (adjacent ranks)
        verify(memberRepository, never()).incrementRanks(anyInt(), anyInt());
        verify(memberRepository, times(2)).save(any(Member.class));
    }
    
    @Test
    void recordMatch_WhenLowerRankedWins_ShouldUpdateRanks() {
        // Arrange - 5 rank difference (8-3), so lower should move up by 2 (5/2)
        when(memberRepository.findById(1L)).thenReturn(Optional.of(higherRanked));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(lowerRanked));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);
        doNothing().when(memberRepository).decrementRanks(3, 4);
        doNothing().when(memberRepository).incrementRanks(6, 8);
        
        // Act
        Game result = rankingService.recordMatch(1L, 2L, 0, 1);
        
        // Assert
        assertEquals(4, higherRanked.getRank()); // Moved down one position
        assertEquals(6, lowerRanked.getRank());  // Moved up by 2 positions (8-6)
        verify(memberRepository).decrementRanks(3, 4);
        verify(memberRepository).incrementRanks(6, 8);
        verify(memberRepository, times(2)).save(any(Member.class));
    }
    
    @Test
    void recordMatch_WhenLowerRankedWinsWithSmallDifference_ShouldNotChangeRanks() {
        // Arrange - adjacent ranks (difference = 1)
        Member adjacentLower = new Member("Adjacent", "Player", "adjacent@email.com", 
                                        LocalDate.of(1990, 8, 22), 4);
        adjacentLower.setId(3L);
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(higherRanked));
        when(memberRepository.findById(3L)).thenReturn(Optional.of(adjacentLower));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);
        
        // Act
        Game result = rankingService.recordMatch(1L, 3L, 0, 1);
        
        // Assert - difference = 1, so moveUp = 0 (no change)
        assertEquals(3, higherRanked.getRank()); // No change
        assertEquals(4, adjacentLower.getRank()); // No change
        verify(memberRepository, never()).decrementRanks(anyInt(), anyInt());
        verify(memberRepository, never()).incrementRanks(anyInt(), anyInt());
        verify(memberRepository, times(2)).save(any(Member.class));
    }
    
    @Test
    void recordMatch_WhenSamePlayer_ShouldThrowException() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            rankingService.recordMatch(1L, 1L, 1, 0);
        });
        verify(memberRepository, never()).findById(any());
        verify(memberRepository, never()).save(any());
        verify(gameRepository, never()).save(any());
    }
    
    @Test
    void recordMatch_WhenPlayerNotFound_ShouldThrowException() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            rankingService.recordMatch(1L, 2L, 1, 0);
        });
        verify(memberRepository).findById(1L);
        verify(memberRepository, never()).save(any());
        verify(gameRepository, never()).save(any());
    }
    
    @Test
    void getGameHistory_ShouldReturnAllGames() {
        // Arrange
        List<Game> expectedGames = Arrays.asList(testGame);
        when(gameRepository.findAllByOrderByPlayedAtDesc()).thenReturn(expectedGames);
        
        // Act
        List<Game> result = rankingService.getGameHistory();
        
        // Assert
        assertEquals(1, result.size());
        verify(gameRepository).findAllByOrderByPlayedAtDesc();
    }
    
    @Test
    void getPlayerGameHistory_ShouldReturnPlayerGames() {
        // Arrange
        List<Game> expectedGames = Arrays.asList(testGame);
        when(gameRepository.findMatchesByPlayerId(1L)).thenReturn(expectedGames);
        
        // Act
        List<Game> result = rankingService.getPlayerGameHistory(1L);
        
        // Assert
        assertEquals(1, result.size());
        verify(gameRepository).findMatchesByPlayerId(1L);
    }
    
    @Test
    void processRankingChanges_ShouldSetCorrectRankAfterValues() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(higherRanked));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(lowerRanked));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game savedGame = invocation.getArgument(0);
            savedGame.setId(1L);
            return savedGame;
        });
        
        // Act
        Game result = rankingService.recordMatch(1L, 2L, 0, 1);
        
        // Assert - Verify that rank after values are set correctly
        assertEquals(4, result.getPlayer1RankAfter()); // higher ranked moved down
        assertEquals(6, result.getPlayer2RankAfter()); // lower ranked moved up
        assertEquals(3, result.getPlayer1RankBefore()); // original rank
        assertEquals(8, result.getPlayer2RankBefore()); // original rank
    }
}