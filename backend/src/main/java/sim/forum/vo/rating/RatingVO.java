package sim.forum.vo.rating;

import lombok.Data;
import sim.forum.entity.Rating;
@Data
public class RatingVO extends Rating {
    // 新的喜欢总数
    private Integer likeCount;
}
