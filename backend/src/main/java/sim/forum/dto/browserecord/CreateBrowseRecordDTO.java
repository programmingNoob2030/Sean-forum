package sim.forum.dto.browserecord;

import lombok.Data;
import sim.forum.entity.BrowseRecord;

@Data
public class CreateBrowseRecordDTO {
    // 目标的类型
    private BrowseRecord.BrowseTarget target;

    // 目标的ID
    private Long targetId;
}
