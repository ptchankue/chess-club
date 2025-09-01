package za.co.tangentsolutions.chessclub.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import za.co.tangentsolutions.chessclub.models.Member;
import za.co.tangentsolutions.chessclub.repositories.MemberRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class APIController {
    private final MemberRepository memberRepository;

    @Autowired
    public APIController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    private static final Logger logger = LogManager.getLogger(APIController.class);

    @GetMapping("/api/members")
    public ResponseEntity<List<Member>> allMembers() {
        logger.info("Getting all club members");
        List<Member> memberList = memberRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(memberList);
    }
    @PostMapping("/api/members")
    public ResponseEntity<Map> addMember(RequestBody member) {
        Map<String, String> resultMap = new HashMap<>();
        logger.info("creating member");
        return ResponseEntity.status(HttpStatus.CREATED).body(resultMap);
    }

}