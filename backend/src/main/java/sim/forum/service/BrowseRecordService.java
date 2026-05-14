package sim.forum.service;


import sim.forum.dto.browserecord.CreateBrowseRecordDTO;
import sim.forum.entity.BrowseRecord;

import java.util.List;

public interface BrowseRecordService {

    /**
     * 通用的异步记录方法
     * targetType 类型：'POST' 或 'BOARD'
     */
    void saveRecordAsync(CreateBrowseRecordDTO dto, Long userId);
    List<Long> getIdsFromRedis(BrowseRecord.BrowseTarget target, Long userId);

}
