package sim.forum.controller;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sim.forum.context.UserContext;
import sim.forum.entity.Rating;
import sim.forum.result.Result;
import sim.forum.dto.rating.RatingDTO;
import sim.forum.service.RatingService;

@RestController
public class RatingController {

    @Autowired
    private RatingService ratingService;
    @PutMapping("/ratings")
    public Result<Rating> toggleRating(@Valid @RequestBody RatingDTO dto){
        Long userId = UserContext.getUserId();
        Rating rating = ratingService.handleVote(dto, userId);
        return Result.success(rating);
    }

}
