package sim.forum.event.comment;

import sim.forum.entity.Comment;

public record CommentRestoreEvent (Long targetId, Comment.CommentTarget target){
}
