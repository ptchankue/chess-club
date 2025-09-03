package za.co.tangentsolutions.chessclub.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import za.co.tangentsolutions.chessclub.models.Game;
import za.co.tangentsolutions.chessclub.models.Member;
import za.co.tangentsolutions.chessclub.services.MemberService;
import za.co.tangentsolutions.chessclub.services.RankingService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(APIController.class)
class APIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private RankingService rankingService;

    @Autowired
    private ObjectMapper objectMapper;

    private Member testMember;
    private Game testGame;

    @BeforeEach
    void setUp() {
        testMember = new Member("John", "Doe", "john.doe@example.com", LocalDate.of(1990, 1, 1), 1);
        testMember.setId(1L);
        testMember.setGamesPlayed(0);

        Member player2 = new Member("Jane", "Smith", "jane.smith@example.com", LocalDate.of(1992, 5, 15), 2);
        player2.setId(2L);
        player2.setGamesPlayed(0);

        testGame = new Game(testMember, player2, 1, 0);
        testGame.setId(1L);
    }

    @Test
    void getAllMembers_ShouldReturnMembersList() throws Exception {
        when(memberService.getAllMembers()).thenReturn(Collections.singletonList(testMember));

        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John"))
                .andExpect(jsonPath("$[0].surname").value("Doe"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));
    }

    @Test
    void getMemberById_WhenMemberExists_ShouldReturnMember() throws Exception {
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(testMember));

        mockMvc.perform(get("/api/members/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"));
    }

    @Test
    void getMemberById_WhenMemberNotFound_ShouldReturn404() throws Exception {
        when(memberService.getMemberById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/members/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addMember_WithValidData_ShouldReturnCreatedMember() throws Exception {
        Member newMember = new Member("Alice", "Johnson", "alice.johnson@example.com", LocalDate.of(1995, 3, 20), 3);
        newMember.setId(3L);
        newMember.setGamesPlayed(0);

        when(memberService.createMember(any(Member.class))).thenReturn(newMember);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newMember)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.surname").value("Johnson"));
    }

    @Test
    void addMember_WithInvalidData_ShouldReturn400() throws Exception {
        Member invalidMember = new Member("", "", "invalid-email", LocalDate.now(), 1);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidMember)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMember_WhenMemberExists_ShouldReturnUpdatedMember() throws Exception {
        testMember.setName("Johnny");
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(testMember));
        when(memberService.updateMember(anyLong(), any(Member.class))).thenReturn(testMember);

        mockMvc.perform(put("/api/members/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testMember)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Johnny"));
    }

    @Test
    void updateMember_WhenMemberNotFound_ShouldReturn404() throws Exception {
        when(memberService.getMemberById(999L)).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Member not found"))
                .when(memberService).updateMember(anyLong(), any(Member.class));

        mockMvc.perform(put("/api/members/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testMember)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMember_WhenMemberExists_ShouldReturn204() throws Exception {
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(testMember));

        mockMvc.perform(delete("/api/members/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMember_WhenMemberNotFound_ShouldReturn404() throws Exception {
        when(memberService.getMemberById(999L)).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Member not found"))
                .when(memberService).deleteMember(anyLong());

        mockMvc.perform(delete("/api/members/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addGame_WithValidData_ShouldReturnCreatedGame() throws Exception {
        when(rankingService.recordMatch(anyLong(), anyLong(), any(Integer.class), any(Integer.class)))
                .thenReturn(testGame);

        String gameRequest = """
                {
                    "player1": {"id": 1},
                    "player2": {"id": 2},
                    "player1Score": 1,
                    "player2Score": 0
                }
                """;

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void addGame_WithInvalidData_ShouldReturn400() throws Exception {
        String invalidGameRequest = """
                {
                    "player1": {"id": 1},
                    "player2": {"id": 1},
                    "player1Score": 1,
                    "player2Score": 0
                }
                """;

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidGameRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllGames_ShouldReturnGamesList() throws Exception {
        when(rankingService.allMatches()).thenReturn(Collections.singletonList(testGame));

        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getPlayerGameHistory_WhenPlayerExists_ShouldReturnGames() throws Exception {
        when(rankingService.getPlayerGameHistory(1L)).thenReturn(Collections.singletonList(testGame));

        mockMvc.perform(get("/api/matches/player/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
