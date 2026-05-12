package sim.forum.dto.rating;

import lombok.Data;
import sim.forum.entity.Rating;

@Data
public class RatingDTO {
    // 评价的对象
    private Rating.RatingType target;

    // 评价的的对象ID
    private Long targetId;

    // 发起人的行为
    private Integer action;

}
