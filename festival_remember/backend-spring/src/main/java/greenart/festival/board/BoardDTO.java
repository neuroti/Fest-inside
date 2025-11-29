package greenart.festival.board;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class BoardDTO {

    @JsonProperty("board_id") // JSON의 board_id와 매핑
    private Long boardId;
    private String title;
    private String content;
    private String period;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String contact;
    private String imageUrl;



}
