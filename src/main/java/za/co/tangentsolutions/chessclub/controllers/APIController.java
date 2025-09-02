package za.co.tangentsolutions.chessclub.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.tangentsolutions.chessclub.models.Game;
import za.co.tangentsolutions.chessclub.models.Member;
import za.co.tangentsolutions.chessclub.services.MemberService;
import za.co.tangentsolutions.chessclub.services.RankingService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@Tag(name = "Chess Club API", description = "API for managing chess club members, games, and rankings")
public class APIController {

    private final MemberService memberService;
    private final RankingService rankingService;

    @Autowired
    public APIController(MemberService memberService, RankingService rankingService) {
        this.memberService = memberService;
        this.rankingService = rankingService;
    }

    private static final Logger logger = LogManager.getLogger(APIController.class);

    @GetMapping("/members")
    @Operation(summary = "Get all members", description = "Retrieve a list of all chess club members ordered by rank")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved members",
                    content = @Content(schema = @Schema(implementation = Member.class)))
    })
    public ResponseEntity<List<Member>> allMembers() {
        logger.info("Getting all club members");
        List<Member> memberList = memberService.getAllMembers();
        return ResponseEntity.status(HttpStatus.OK).body(memberList);
    }

    @GetMapping("/members/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        logger.info("Getting member by id: {}", id);
        Optional<Member> member = memberService.getMemberById(id);
        return member.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/members")
    @Operation(summary = "Add a new member", description = "Create a new chess club member")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Member created successfully",
                    content = @Content(schema = @Schema(implementation = Member.class))),
        @ApiResponse(responseCode = "400", description = "Invalid member data")
    })
    public ResponseEntity<Object> addMember(@Valid @RequestBody Member member) {
        Member newMember = memberService.createMember(member);
        logger.info("creating member");
        return ResponseEntity.status(HttpStatus.CREATED).body(newMember);
    }

    @PutMapping("/members/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @Valid @RequestBody Member memberDetails) {
        logger.info("Updating member with id: {}", id);
        try {
            Member updatedMember = memberService.updateMember(id, memberDetails);
            return ResponseEntity.ok(updatedMember);
        } catch (RuntimeException e) {
            logger.error("Error updating member: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/members/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        logger.info("Deleting member with id: {}", id);
        try {
            memberService.deleteMember(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Error deleting member: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/matches")
    public ResponseEntity<List<Game>> allMatches() {
        logger.info("Getting all club matches");
        List<Game> matchesList = rankingService.allMatches();
        return ResponseEntity.status(HttpStatus.OK).body(matchesList);
    }

    @PostMapping("/matches")
    @Operation(summary = "Record a match", description = "Record a chess match between two players and update rankings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Match recorded successfully",
                    content = @Content(schema = @Schema(implementation = Game.class))),
        @ApiResponse(responseCode = "400", description = "Invalid match data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Object> addGame(@Valid @RequestBody Game game) {
        try {
            // Validate that players are different
            if (game.getPlayer1().getId().equals(game.getPlayer2().getId())) {
                return ResponseEntity.badRequest().body("Player 1 and Player 2 must be different");
            }
            
            Game newGame = rankingService.recordMatch(
                    game.getPlayer1().getId(),
                    game.getPlayer2().getId(),
                    game.getPlayer1Score(),
                    game.getPlayer2Score()
            );
            logger.info("creating game");
            return ResponseEntity.status(HttpStatus.CREATED).body(newGame);
        } catch (Exception e) {
            logger.error("Error creating game: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating game: " + e.getMessage());
        }
    }

    @GetMapping("/matches/player/{playerId}")
    @Operation(summary = "Get player game history", description = "Retrieve all games for a specific player")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved player games",
                    content = @Content(schema = @Schema(implementation = Game.class)))
    })
    public ResponseEntity<List<Game>> getPlayerGameHistory(@PathVariable Long playerId) {
        logger.info("Getting game history for player: {}", playerId);
        List<Game> games = rankingService.getPlayerGameHistory(playerId);
        return ResponseEntity.ok(games);
    }

}