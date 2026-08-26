package sim.forum.dto.post.content;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PostContentDocument {
    private Integer version;
    private PostContentFormat format;
    private List<PostContentNode> nodes = new ArrayList<>();
    private String textPreview;
    private String firstImagePath;
    private int textCodePointCount;
}
