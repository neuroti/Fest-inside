package greenart.festival.recommend.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecommendationResponse {

    private Long user_id;
    private List<Long> recommendations;

}
