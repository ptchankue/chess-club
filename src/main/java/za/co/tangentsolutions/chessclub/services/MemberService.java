package za.co.tangentsolutions.chessclub.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.tangentsolutions.chessclub.models.Match;
import za.co.tangentsolutions.chessclub.models.Member;
import za.co.tangentsolutions.chessclub.repositories.MemberRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {
    

    private final MemberRepository memberRepository;
    private final RankingService rankingService;


    @Autowired
    public MemberService(MemberRepository memberRepository, RankingService rankingService) {
        this.memberRepository = memberRepository;
        this.rankingService = rankingService;
    }


    public List<Member> getAllMembers() {
        return memberRepository.findAllByOrderByRankAsc();
    }
    
    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(id);
    }

    @Transactional
    public Member createMember(Member member) {
        // Set initial rank (last position)
        Integer maxRank = memberRepository.findMaxRank();
        int newRank = (maxRank != null) ? maxRank + 1 : 1;
        member.setRank(newRank);

        return memberRepository.save(member);
    }


    @Transactional
    public Member updateMember(Long id, Member memberDetails) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        
        member.setName(memberDetails.getName());
        member.setSurname(memberDetails.getSurname());
        member.setEmail(memberDetails.getEmail());
        member.setBirthday(memberDetails.getBirthday());
        
        return memberRepository.save(member);
    }
    
    @Transactional
    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        
        int deletedRank = member.getRank();
        memberRepository.delete(member);
        
        // Update ranks of remaining members
        memberRepository.decrementRanks(deletedRank, Integer.MAX_VALUE);
    }

    public List<Match> getPlayerGameHistory(Long memberId) {
        return rankingService.getPlayerGameHistory(memberId);
    }
}