package za.co.tangentsolutions.chessclub.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "player1_id", nullable = false)
    private Member player1;
    
    @ManyToOne
    @JoinColumn(name = "player2_id", nullable = false)
    private Member player2;
    
    @Column(name = "player1_score", nullable = false)
    private int player1Score;
    
    @Column(name = "player2_score", nullable = false)
    private int player2Score;
    
    @Column(nullable = false)
    private LocalDateTime playedAt;
    
    @Column(name = "player1_rank_before")
    private int player1RankBefore;
    
    @Column(name = "player2_rank_before")
    private int player2RankBefore;
    
    @Column(name = "player1_rank_after")
    private int player1RankAfter;
    
    @Column(name = "player2_rank_after")
    private int player2RankAfter;
    
    // Constructors
    public Game() {
        this.playedAt = LocalDateTime.now();
    }
    
    public Game(Member player1, Member player2, int player1Score, int player2Score) {
        this();
        this.player1 = player1;
        this.player2 = player2;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
        this.player1RankBefore = player1.getRank();
        this.player2RankBefore = player2.getRank();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Member getPlayer1() { return player1; }
    public void setPlayer1(Member player1) { this.player1 = player1; }
    
    public Member getPlayer2() { return player2; }
    public void setPlayer2(Member player2) { this.player2 = player2; }
    
    public int getPlayer1Score() { return player1Score; }
    public void setPlayer1Score(int player1Score) { this.player1Score = player1Score; }
    
    public int getPlayer2Score() { return player2Score; }
    public void setPlayer2Score(int player2Score) { this.player2Score = player2Score; }
    
    public LocalDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }
    
    public int getPlayer1RankBefore() { return player1RankBefore; }
    public void setPlayer1RankBefore(int player1RankBefore) { this.player1RankBefore = player1RankBefore; }
    
    public int getPlayer2RankBefore() { return player2RankBefore; }
    public void setPlayer2RankBefore(int player2RankBefore) { this.player2RankBefore = player2RankBefore; }
    
    public int getPlayer1RankAfter() { return player1RankAfter; }
    public void setPlayer1RankAfter(int player1RankAfter) { this.player1RankAfter = player1RankAfter; }
    
    public int getPlayer2RankAfter() { return player2RankAfter; }
    public void setPlayer2RankAfter(int player2RankAfter) { this.player2RankAfter = player2RankAfter; }
    
    // Helper methods
    public boolean isDraw() {
        return player1Score == player2Score;
    }
    
    public Member getWinner() {
        if (player1Score > player2Score) return player1;
        if (player2Score > player1Score) return player2;
        return null; // Draw
    }
    
    public Member getLoser() {
        if (player1Score < player2Score) return player1;
        if (player2Score < player1Score) return player2;
        return null; // Draw
    }
}