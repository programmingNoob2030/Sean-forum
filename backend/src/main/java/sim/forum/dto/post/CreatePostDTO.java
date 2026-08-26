package sim.forum.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePostDTO {
    // 发帖人的ID(无需自定义，通过前端Token获得)
//    private Long postCreator;

    @NotNull(message = "帖子所属的社区不能为空")
    // 帖子属于的社区
    private Long boardId;
    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 100, message = "帖子标题不能超过100个字符")
    // 帖子的标题
    private String title;
    @NotBlank(message = "帖子内容不能为空")
    // 帖子的内容
    private String content;
}
