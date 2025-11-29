package greenart.festival.recommend.cotroller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import greenart.festival.board.BoardDTO;
import greenart.festival.board.BoardRepository;
import greenart.festival.connection.service.FastApiClient;
import greenart.festival.member.entity.Member;
import greenart.festival.member.repository.MemberRepository;
import greenart.festival.recommend.dto.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/festival")
@RequiredArgsConstructor
public class RecommendController {

    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final FastApiClient fastApiClient;
    private final ObjectMapper objectMapper;



    @GetMapping("/recommend")
    public String recommend(Principal principal, Model model) {
        String email = principal.getName();
        Member member = memberRepository.findByEmail(email).get();
        Long userId = member.getId();


        // Hybrid Recommendation 호출
        String hybridResponse = fastApiClient.recommendParameters(userId, 3, 0.5).block();

        RecommendationResponse hybridRecommendationResponse;
        try {

            hybridRecommendationResponse = objectMapper.readValue(hybridResponse, RecommendationResponse.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse hybridResponse: " + hybridResponse, e);
        }

        List<Long> hybridRecommendations  = hybridRecommendationResponse.getRecommendations();
        List<BoardDTO> hybridRecommendList = hybridRecommendations.stream()
                .map(boardRepository::findByBoardId)
                .collect(Collectors.toList());


        model.addAttribute("userId", userId);
        model.addAttribute("recommendList", hybridRecommendList);


        // MBTI별 추천 리스트
        Map<String, List<BoardDTO>> mbtiRecommendMap = new HashMap<>();
        List<String> mbtiTypes = Arrays.asList("ISTJ", "ISFJ", "INFJ", "INTJ", "ISTP", "ISFP", "INFP", "INTP", "ESTP", "ESFP", "ENFP", "ENTP", "ESTJ", "ESFJ", "ENFJ", "ENTJ"); // MBTI 타입 추가

        for (String mbti : mbtiTypes) {
            String mbtiResponse = fastApiClient.mbtiParameters(mbti, 5).block();
            List<BoardDTO> mbtiRecommendList;

            try {
                mbtiRecommendList = objectMapper.readValue(
                        mbtiResponse,
                        new TypeReference<List<BoardDTO>>() {}
                );

                for (BoardDTO boardDTO : mbtiRecommendList) {
                    BoardDTO boardDTOImage = boardRepository.findByBoardId(boardDTO.getBoardId());

                    boardDTO.setImageUrl(boardDTOImage.getImageUrl());
                }
                System.out.println("mbtiRecommendList = " + mbtiRecommendList);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse mbtiResponse: " + mbtiResponse, e);
            }

            mbtiRecommendMap.put(mbti, mbtiRecommendList);
        }

        // 추천 리스트를 모델에 추가
        model.addAttribute("mbtiRecommendMap", mbtiRecommendMap);

        return "/board/recommend";
    }
}
