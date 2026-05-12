package sim.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sim.forum.entity.BoardMember;

@Mapper
public interface BoardMemberMapper extends BaseMapper<BoardMember> {
}
