package sim.forum.result; // 根据你的实际包名修改

import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 分页返回结果包装类
 * 专门用于 Result 类的 data 属性中
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 总记录数（给前端展示“共 8888 条”）
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 快捷静态方法：将 MyBatis 的 PageInfo 转换为 PageResult
     * 这样做可以避免在 Service 层手动写一堆构造函数
     */
    public static <T> PageResult<T> of(PageInfo<T> pageInfo) {
        return new PageResult<>(
                pageInfo.getList(),
                pageInfo.getTotal(),
                pageInfo.getPageNum(),
                pageInfo.getPageSize()
        );
    }
}