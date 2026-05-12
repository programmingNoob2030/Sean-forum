package sim.forum.event.comment;

import sim.forum.entity.Comment;

public record CommentDeleteEvent (Long targetId, Comment.CommentTarget target){
}
