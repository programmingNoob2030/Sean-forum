package sim.forum.dto.post.content;

import lombok.Data;

@Data
public class PostContentNode {
    private PostContentNodeType type;
    private String text;
    private String path;
}
