package sim.forum.event.rating;

import sim.forum.entity.Rating;

public record ToggleRatingEvent (Long targetId, Rating.RatingType target, Integer delta){ }
