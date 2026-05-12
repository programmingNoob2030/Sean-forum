package sim.forum.service;

import sim.forum.dto.rating.RatingDTO;
import sim.forum.entity.Rating;

public interface RatingService {
    Rating handleVote(RatingDTO dto);

    Rating selectById(Long id);


}
