package za.co.tangentsolutions.chessclub.controllers;

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

@RestController
@RequestMapping("/api")
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
    public ResponseEntity<List<Member>> allMembers() {
        logger.info("Getting all club members");
        List<Member> memberList = memberService.getAllMembers();
        return ResponseEntity.status(HttpStatus.OK).body(memberList);
    }
    @PostMapping("/members")
    public ResponseEntity<Object> addMember(@Valid @RequestBody Member member) {

        Member newMember = memberService.createMember(member);
        logger.info("creating member");
        return ResponseEntity.status(HttpStatus.CREATED).body(newMember);
    }


    @GetMapping("/matches")
    public ResponseEntity<List<Game>> allMatches() {
        logger.info("Getting all club matches");
        List<Game> matchesList = rankingService.allMatches();
        return ResponseEntity.status(HttpStatus.OK).body(matchesList);
    }

    @PostMapping("/matches")
    public ResponseEntity<Object> addGame(@Valid @RequestBody Game game) {
        try {
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

}