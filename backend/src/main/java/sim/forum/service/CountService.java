package sim.forum.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CountService {

    /**
     * 通用原子计数核心方法
     * @param mapper 传入对应的 Mapper 实例（比如在 UserServiceImpl 里传 baseMapper）
     * @param id 业务主键
     * @param column 数据库字段名（如 "post_count"）
     * @param isIncrement true 为 +1, false 为 -1
     */
    public <T> void updateAtomicCount(BaseMapper<T> mapper, Long id, String column, Integer delta, boolean isIncrement) {
        String operator = isIncrement ? "+" : "-";

        UpdateWrapper<T> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id);

        // 核心 SQL 拼接：使用 IFNULL 防止空指针，并进行原子加减
        String sql = String.format("%s = IFNULL(%s, 0) %s %s", column, column, operator, delta.toString());
        wrapper.setSql(sql);

        // 如果是减法（比如取消点赞），增加一个“大于 0”的判断，防止数据库出现负数
        if (!isIncrement) {
            wrapper.gt(column, 0);
        }

        int rows = mapper.update(null, wrapper);

        if (rows == 0) {
            log.warn("原子计数未命中！可能数据不存在或尝试减至负数。ID: {}, 字段: {}", id, column);
            // 这种一般属于业务异常，建议抛出，让外层的 @Transactional 能够回滚
            throw new RuntimeException("操作失败：数据状态异常");
        }
    }
}
