package greenart.festival.board;

import com.fasterxml.jackson.databind.ObjectMapper;
import greenart.festival.bookmark.dto.BoardBookmarkedDTO;
import greenart.festival.connection.service.FastApiClient;
import greenart.festival.connection.service.FastApiService;
import greenart.festival.member.entity.Member;
import greenart.festival.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/festival")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final FastApiService fastApiService;
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final FastApiClient fastApiClient;
    private final ObjectMapper objectMapper;


    @GetMapping("/map")
    public String map(@RequestParam(required = false) String keyword, Principal principal, Model model) {

        String email = principal.getName();
        if (keyword == null || keyword.isEmpty()) {
            model.addAttribute("festivalList", boardService.getAllList(email));
        } else {
            // 키워드로 필터링된 데이터 반환
            model.addAttribute("festivalList", boardService.searchFestivals(keyword));
        }

        // 검색 키워드도 뷰로 전달
        model.addAttribute("keyword", keyword);

        return "board/map";
    }

//    @GetMapping("/map/{boardId}/bookmark")
//    @ResponseBody
//    public boolean bookmark(@PathVariable Long boardId, @AuthenticationPrincipal MemberAuthDTO memberAuthDTO) {
//        Member member = memberService.dtoToEntity(memberAuthDTO);
//
//        return bookmarkService.bookmarking(member.getEmail(), boardId);
//    }



    @GetMapping("/details/{boardId}")
    public String details(@PathVariable Long boardId, Principal principal, Model model, RedirectAttributes redirectAttributes) {
        String email = principal.getName();
        BoardBookmarkedDTO boardDTO = boardService.getBookmarkedBoard(boardId, email);

        model.addAttribute("board", boardDTO);

        Member member = memberRepository.findByEmail(email).get();
        String idName = member.getName();

        model.addAttribute("idName", idName);

        boardService.increaseViewCount(boardDTO.getBoardId()); // 조회수 증가

        // FastAPI에 전달할 파라미터
        String searchKeyword = boardDTO.getTitle(); // 제목
        String regionCode = getRegionCode(boardDTO.getLocation()); // 장소

        // FastAPI에 요청 보내기
        String fastApiResponse = fastApiService.getFastApiResponse(searchKeyword, regionCode);
        model.addAttribute("fastApiResponse", fastApiResponse);
        return "board/details";

//        // 리다이렉트하면서 파라미터 전달
//        redirectAttributes.addAttribute("searchKeyword", searchKeyword);
//        redirectAttributes.addAttribute("regionCode", regionCode);
//
//        return "redirect:/festival/detail";
    }


    private String getRegionCode(String location) {
        // 지역 코드 매핑
        Map<String, String> regionCodeMap = new HashMap<>();
        regionCodeMap.put("서울시", "01");
        regionCodeMap.put("부산시", "02");
        regionCodeMap.put("대구시", "03");
        regionCodeMap.put("인천시", "04");
        regionCodeMap.put("광주시", "05");
        regionCodeMap.put("대전시", "06");
        regionCodeMap.put("울산시", "07");
        regionCodeMap.put("세종시", "08");
        regionCodeMap.put("경기도", "10");
        regionCodeMap.put("강원도", "11");
        regionCodeMap.put("충청북도", "12");
        regionCodeMap.put("충청남도", "13");
        regionCodeMap.put("전라북도", "14");
        regionCodeMap.put("전라남도", "15");
        regionCodeMap.put("경상북도", "16");
        regionCodeMap.put("경상남도", "17");
        regionCodeMap.put("제주도", "18");
        return regionCodeMap.entrySet().stream()
                .filter(entry -> location != null && location.startsWith(entry.getKey()))
                .map(Map.Entry::getValue).findFirst().orElse("");
    }
}
