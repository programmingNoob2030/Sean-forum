package sim.forum.dto.comment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteCommentDTO {
    @NotNull(message = "ID不能为空")
    @Min(value = 1, message = "ID必须大于0")
    /* 评论的ID */
    private Long id;
}
