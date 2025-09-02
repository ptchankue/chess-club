package za.co.tangentsolutions.chessclub.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.co.tangentsolutions.chessclub.models.Member;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    List<Member> findAllByOrderByRankAsc();
    
    @Query("SELECT MAX(m.rank) FROM Member m")
    Integer findMaxRank();
    
    @Modifying
    @Query("UPDATE Member m SET m.rank = m.rank + 1 WHERE m.rank >= :startRank AND m.rank < :endRank")
    void incrementRanks(@Param("startRank") int startRank, @Param("endRank") int endRank);
    
    @Modifying
    @Query("UPDATE Member m SET m.rank = m.rank - 1 WHERE m.rank > :startRank AND m.rank <= :endRank")
    void decrementRanks(@Param("startRank") int startRank, @Param("endRank") int endRank);

    boolean existsByEmail(String email);
}