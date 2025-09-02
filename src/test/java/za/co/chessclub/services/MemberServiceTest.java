package za.co.chessclub.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.tangentsolutions.chessclub.models.Member;
import za.co.tangentsolutions.chessclub.repositories.MemberRepository;
import za.co.tangentsolutions.chessclub.services.MemberService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.helger.commons.mock.CommonsAssert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

//    @Mock
//    private RankingService rankingService;

    @InjectMocks
    private MemberService memberService;

    private Member testMember;
    private Member testMember2;

    @BeforeEach
    void setUp() {
        testMember = new Member("John", "Doe", "john@email.com",
                LocalDate.of(1985, 5, 15), 1);
        testMember.setId(1L);
        testMember.setGamesPlayed(10);

        testMember2 = new Member("Jane", "Smith", "jane@email.com",
                LocalDate.of(1990, 8, 22), 2);
        testMember2.setId(2L);
        testMember2.setGamesPlayed(8);
    }

    @Test
    void getAllMembers_ShouldReturnAllMembers() {
        // Arrange
        List<Member> expectedMembers = Arrays.asList(testMember, testMember2);
        when(memberRepository.findAllByOrderByRankAsc()).thenReturn(expectedMembers);

        // Act
        List<Member> result = memberService.getAllMembers();

        // Assert
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getName());
        assertEquals("Jane", result.get(1).getName());
        verify(memberRepository).findAllByOrderByRankAsc();
    }

    @Test
    void getMemberById_WhenMemberExists_ShouldReturnMember() {
        // Arrange
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        // Act
        Optional<Member> result = memberService.getMemberById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getName());
        verify(memberRepository).findById(1L);
    }

    @Test
    void getMemberById_WhenMemberNotExists_ShouldReturnEmpty() {
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Member> result = memberService.getMemberById(999L);

        assertFalse(result.isPresent());
        verify(memberRepository).findById(999L);
    }

    @Test
    void createMember_WhenNoExistingMembers_ShouldSetRankToOne() {
        Member newMember = new Member("New", "Member", "new@email.com",
                LocalDate.of(2000, 1, 1), 0);
        when(memberRepository.findMaxRank()).thenReturn(null);
        when(memberRepository.save(any(Member.class))).thenReturn(newMember);

        Member result = memberService.createMember(newMember);

        assertEquals(1, result.getRank());
        verify(memberRepository).findMaxRank();
        verify(memberRepository).save(newMember);
    }

    @Test
    void createMember_WithExistingMembers_ShouldSetRankToLast() {
        Member newMember = new Member("New", "Member", "new@email.com",
                LocalDate.of(2000, 1, 1), 0);
        when(memberRepository.findMaxRank()).thenReturn(5);
        when(memberRepository.save(any(Member.class))).thenReturn(newMember);

        Member result = memberService.createMember(newMember);

        assertEquals(6, result.getRank());
        verify(memberRepository).findMaxRank();
        verify(memberRepository).save(newMember);
    }

}
