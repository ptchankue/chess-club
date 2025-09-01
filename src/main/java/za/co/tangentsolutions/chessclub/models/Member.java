package za.co.tangentsolutions.chessclub.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String surname;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private LocalDate birthday;
    
    @Column(name = "games_played", nullable = false)
    private int gamesPlayed = 0;
    
    @Column(nullable = false)
    private int rank;
    
    // Constructors
    public Member() {}
    
    public Member(String name, String surname, String email, LocalDate birthday, int rank) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.birthday = birthday;
        this.rank = rank;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
    
    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }
    
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    
    public String getFullName() {
        return name + " " + surname;
    }
}