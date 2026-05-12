package sim.forum.dto.post;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeletePostDTO {

    @NotNull(message = "ID不能为空")
    @Min(value = 1, message = "ID必须大于0")
    /* 帖子的ID */
    private Long id;
}
