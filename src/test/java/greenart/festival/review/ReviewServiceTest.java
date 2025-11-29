package greenart.festival.review;

import greenart.festival.board.BoardRepository;
import greenart.festival.member.entity.Member;
import greenart.festival.member.entity.Social;
import greenart.festival.member.repository.MemberRepository;
import greenart.festival.review.dto.CommentDTO;
import greenart.festival.review.dto.CommentReplyDTO;
import greenart.festival.review.dto.ReviewDTO;
import greenart.festival.review.dto.ReviewResponseDTO;
import greenart.festival.review.repository.ReviewRepository;
import greenart.festival.review.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@SpringBootTest
class ReviewServiceTest {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private MemberRepository memberRepository;

    @Test //등록 테스트
    void addReview() {
        Random random = new Random();


        String email = "user1@example.com";
        Member member = memberRepository.findByEmail(email).get();

        System.out.println("member = " + member);
        for (int j = 1; j <= 20 ; j++) {
            Double rating = (double)(1 + random.nextInt(5));
            Long boardId = 1 +  random.nextLong(50);

            ReviewDTO reviewDTO = new ReviewDTO("test"+j, rating);
            reviewService.addReview(reviewDTO, boardId, member);
        }

    }

    @Test //수정 테스트
    void modifyReview() {

        Member member = memberRepository.findByEmail("mpi9904@gmail.com",Social.NONE).get();

        ReviewDTO reviewDTO = new ReviewDTO("modified test4",3.7);

        reviewService.modifyReview(reviewDTO, 4L, member);
        System.out.println(reviewRepository.findById(4L).get());
    }

    @Test //조회 테스트
    void getReviews() {

        List<CommentReplyDTO> reviews = reviewService.getReviews(7L);
        System.out.println("reviews = " + reviews);

    }

    @Test //삭제 테스트
    void deleteReview() {

        Member member = memberRepository.findByEmail("mpi9904@naver.com",Social.NAVER).get();

        reviewService.deleteReview(5L, member);
    }

    @Test // 대댓 테스트
    void addReplyAtReview() {
        for (int i = 1; i <10 ; i++) {
            Member member = memberRepository.findByEmail("mpi9904@gmail.com",Social.NONE).get();

            ReviewDTO reviewDTO = new ReviewDTO("RE review"+i, null);


            reviewService.addReplyAtReview(5L, reviewDTO, member);
        }


    }
//    @Test // 대댓 테스트
//    void deleteReplyAtReview() {
//
//
//            reviewService.deleteReplyAtReview(5L, 31L);
//
//
//    }

    @Test // 별점 평균 테스트
    void getAverageRatingByBoard() {

        Double avg = reviewService.getAverageRatingByBoard(1L);

        System.out.println("avg = " + avg);

    }

    @Test // 게시물 당 후기 카운트 테스트
    void countReviews() {

        Long count = reviewService.getReviewCountByBoard(1L);
        System.out.println("count = " + count);

    }
}