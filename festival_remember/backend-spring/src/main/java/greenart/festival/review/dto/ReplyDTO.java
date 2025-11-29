package greenart.festival.review.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class ReplyDTO {

    Long reviewId;
    Long boardId;
    Long rootNum;
    Long depth;
    String content;
    String email;
    String author;
    LocalDateTime regDate;
}
