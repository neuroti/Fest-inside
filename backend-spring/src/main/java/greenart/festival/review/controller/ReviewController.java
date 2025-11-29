package greenart.festival.review.controller;

import greenart.festival.member.dto.MemberAuthDTO;
import greenart.festival.member.entity.Member;
import greenart.festival.member.entity.Social;
import greenart.festival.member.repository.MemberRepository;
import greenart.festival.member.service.MemberService;
import greenart.festival.review.dto.CommentDTO;
import greenart.festival.review.dto.CommentReplyDTO;
import greenart.festival.review.dto.ReplyDTO;
import greenart.festival.review.dto.ReviewDTO;
import greenart.festival.review.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/festival/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;


    @GetMapping()
    public String index(Model model, Principal principal) {

        if (principal == null) {
            System.out.println("Principal is null");
        }

        String email = principal.getName();

        Member member = memberRepository.findByEmail(email).get();
        System.out.println("member = " + member);

        String name = member.getName();


        model.addAttribute("name", name);


        return "/festival/review";
    }


    @PostMapping("/add/{boardId}")
    @ResponseBody
    public void addReview(@PathVariable Long boardId,
                          @AuthenticationPrincipal MemberAuthDTO memberAuthDTO,
                          @RequestBody ReviewDTO reviewDTO) {

        Member member = memberRepository.findByEmail(memberAuthDTO.getEmail()).get();

        Social provider = member.getProvider();
        System.out.println("provider = " + provider);
        if(member != null) {

            reviewService.addReview(reviewDTO, boardId, member);

        } else{
//            null 검사, "로그인 후 이용해 주세요" 띄우기
              throw new IllegalArgumentException("로그인 후 이용해주세요");
        }
    }

    @GetMapping("/get/{boardId}") //조회
    @ResponseBody
    public ResponseEntity<?> getReviews(@PathVariable Long boardId,
                                        Model model, Principal principal) {

        String email = principal.getName();
        Member member = memberRepository.findByEmail(email).get();
        String identifiedName = member.getName();

        List<CommentReplyDTO> reviewList = reviewService.getReviews(boardId);
        List<CommentReplyDTO> reviews = new ArrayList<>();

        for(CommentReplyDTO comment : reviewList) {
            if(comment.getDepth() != 1){
                reviews.add(comment);
            }
        }

        System.out.println("reviews = " + reviews);
        model.addAttribute("reviews", reviews);
        model.addAttribute("identifiedName", identifiedName);
        System.out.println("identifiedName = " + identifiedName);

        if( reviews == null || reviews.isEmpty() ) {
            return ResponseEntity.ok(Collections.singletonMap("reviews", Collections.emptyList()));
        }
        return ResponseEntity.ok(Collections.singletonMap("reviews", reviews));

    }

    @GetMapping("/view/{rootNum}") //조회
    @ResponseBody
    public ResponseEntity<?> getReplies(@PathVariable Long rootNum,
                                        Model model) {

        List<ReplyDTO> replies = reviewService.getReplies(rootNum);
        model.addAttribute("replies", replies);

        System.out.println("replies = " + replies);

        if( replies == null || replies.isEmpty() ) {
            return ResponseEntity.ok(Collections.singletonMap("replies", Collections.emptyList()));
        }
        return ResponseEntity.ok(Collections.singletonMap("replies", replies));

    }


    @PutMapping("/modify/{reviewId}") //수정
    @ResponseBody
    public String modifyReview(@PathVariable Long reviewId,
                               @AuthenticationPrincipal MemberAuthDTO memberAuthDTO,
                               @RequestBody ReviewDTO reviewDTO) {

        Member member = memberService.dtoToEntity(memberAuthDTO);

        if(member != null) {

            return reviewService.modifyReview(reviewDTO,reviewId,member);
        }

        return "로그인 확인";
    }

    @DeleteMapping("/delete/{reviewId}") //삭제
    @ResponseBody
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId,
                               @AuthenticationPrincipal MemberAuthDTO memberAuthDTO) {
        System.out.println("memberAuthDTO = " + memberAuthDTO);

        if (memberAuthDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인을 확인하여 주시길 바랍니다.");
        }

        Member member = memberService.dtoToEntity(memberAuthDTO);

        try {
            // 댓글 삭제 서비스 호출
            String result = reviewService.deleteReview(reviewId, member);
            return ResponseEntity.ok(result); // 성공 응답
        } catch (IllegalArgumentException e) {
            // 삭제 요청에 잘못된 값
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            // 기타 서버 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("댓글 삭제 중 문제가 발생했습니다.");
        }
    }

//    @GetMapping("/avg/{boardId}") //별점 평균
//    public Double getAverageRatingByBoard(@PathVariable Long boardId, Model model) {
//        Double avgRating = reviewService.getAverageRatingByBoard(boardId);
//        model.addAttribute("avgRating", avgRating);
//        return avgRating;
//    }
//
//    @GetMapping("/count/{boardId}") //게시물당 리뷰 갯수
//    public Long getReviewCountByBoard(@PathVariable Long boardId){
//
//        return reviewService.getReviewCountByBoard(boardId);
//    }

    @GetMapping("/about/{boardId}")
    @ResponseBody
    public Map<String, Object> getReviewAbout(@PathVariable Long boardId) {
        Double avgRating = reviewService.getAverageRatingByBoard(boardId);
        Long reviewCount = reviewService.getReviewCountByBoard(boardId);

        Map<String, Object> response = new HashMap<>();
        response.put("avgRating", avgRating != null ? avgRating : 0.0);
        response.put("reviewCount", reviewCount != null ? reviewCount : 0L);
        return response;
    }


    @PostMapping("/reply/{rootNumId}") //대댓 달기
    @ResponseBody
    public void addReplyAtReview(@PathVariable Long rootNumId,
                                 @RequestBody ReviewDTO reviewDTO,
                                 Principal principal){
        String email = principal.getName();

        Member member = memberRepository.findByEmail(email).get();
        reviewService.addReplyAtReview(rootNumId, reviewDTO, member);
    }
}
