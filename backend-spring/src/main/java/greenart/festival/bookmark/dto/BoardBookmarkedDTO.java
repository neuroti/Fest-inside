package greenart.festival.bookmark.dto;

import lombok.*;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@ToString
public class BoardBookmarkedDTO {
    private Long boardId;
    private String title;
    private String content;
    private String period;
    private String location;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String contact;
    private String imageUrl;
    private Boolean bookmark;


}
