package sim.forum.service;

import sim.forum.dto.rating.RatingDTO;
import sim.forum.entity.Rating;
import sim.forum.vo.rating.RatingVO;

public interface RatingService {
    RatingVO handleVote(RatingDTO dto, Long userId);

    Rating selectById(Long id);


}
