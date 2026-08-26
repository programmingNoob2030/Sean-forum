package sim.forum.vo.post.content;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import sim.forum.dto.post.content.PostContentNodeType;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostContentNodeVO {
    private PostContentNodeType type;
    private String text;
    private String path;
}
