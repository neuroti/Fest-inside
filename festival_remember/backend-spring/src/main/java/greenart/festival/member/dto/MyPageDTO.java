package greenart.festival.member.dto;

import greenart.festival.member.entity.MBTI;
import greenart.festival.member.entity.Social;
import greenart.festival.review.dto.CommentDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
public class MyPageDTO {

    private String email;
    private String name;
    private String password;
    private List<CommentDTO> comments;     // 내가 쓴 댓글 정보 list
    private Social provider; // 소셜 로그인 여부
    private String phoneNumber;
    private LocalDate birthDate;
    private MBTI mbti;


    public MyPageDTO(String email, String name, String password,
                     Social provider, List<CommentDTO> comments, String phoneNumber, LocalDate birthDate, MBTI mbti) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.provider = provider;
        this.comments = comments;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        this.mbti = mbti;
    }

    public Long getId() {
        return 0L;
    }

}