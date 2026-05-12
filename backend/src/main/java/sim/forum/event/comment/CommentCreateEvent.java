package sim.forum.event.comment;
import sim.forum.entity.Comment;

public record CommentCreateEvent(Long creator, Long targetId, Comment.CommentTarget target) {}
