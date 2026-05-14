package sim.forum.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sim.forum.dto.browserecord.CreateBrowseRecordDTO;
import sim.forum.entity.BrowseRecord;
import sim.forum.mapper.BrowseRecordMapper;
import sim.forum.service.BoardService;
import sim.forum.service.BrowseRecordService;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BrowseRecordServiceImpl implements BrowseRecordService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private BrowseRecordMapper browseRecordMapper;
    @Override
    @Async("taskExecutor")
    public void saveRecordAsync(CreateBrowseRecordDTO dto,Long userId) {
        // 这里的 dto.getTarget() 会自动转成 "BOARD" 或 "POST"
        String redisKey = "user:history:" + dto.getTarget() + ":" + userId;
        // 1. Redis 滑动窗口 (保留5条)
        redisTemplate.opsForZSet().add(redisKey, dto.getTargetId().toString(), System.currentTimeMillis());
        int limit = dto.getTarget().getLimit();
        redisTemplate.opsForZSet().removeRange(redisKey, 0, -(limit + 1));
        // 在执行完裁剪后，顺手加一行，保证内存不被僵尸数据占满
        redisTemplate.expire(redisKey, 30, TimeUnit.DAYS);
        // 2. MySQL 流水
        BrowseRecord record = new BrowseRecord();
        record.setUserId(userId);
        record.setTargetId(dto.getTargetId());
        // 这里赋值时，如果实体类是 String，会自动调用枚举的 toString()
        record.setTarget(dto.getTarget());
        browseRecordMapper.insert(record);
    }



    /**
     * 这是一个通用的私有方法，专门负责从 Redis 捞出 ID
     */
    public List<Long> getIdsFromRedis(BrowseRecord.BrowseTarget target, Long userId) {
        String redisKey = "user:history:" + target.name() + ":" + userId;
        // 1. 从最新的开始拿
        Set<String> idSet = redisTemplate.opsForZSet().reverseRange(redisKey, 0, target.getLimit() - 1);

        if (idSet == null || idSet.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 转换成 Long 列表
        return idSet.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}