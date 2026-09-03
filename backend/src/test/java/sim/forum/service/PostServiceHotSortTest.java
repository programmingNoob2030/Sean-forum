package sim.forum.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sim.forum.context.UserContext;
import sim.forum.dto.post.PostQueryDTO;
import sim.forum.dto.post.content.PostContentCodec;
import sim.forum.mapper.PostMapper;
import sim.forum.result.PageResult;
import sim.forum.service.impl.PostServiceImpl;
import sim.forum.vo.post.PostVO;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceHotSortTest {
    @Mock
    private PostMapper postMapper;
    @Mock
    private CountService countService;
    @Mock
    private BrowseRecordService browseRecordService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Spy
    private PostContentCodec postContentCodec = new PostContentCodec(new ObjectMapper());
    @InjectMocks
    private PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        UserContext.setUserId(11L);
    }

    @AfterEach
    void tearDown() {
        UserContext.removeUserId();
    }

    @Test
    void getPostsRoutesHotSortToHotMapperQuery() {
        PostVO postVO = new PostVO();
        postVO.setId(1L);
        postVO.setContent("hot post");
        when(postMapper.getHotPosts(any(PostQueryDTO.class), eq(11L))).thenReturn(List.of(postVO));

        PostQueryDTO dto = new PostQueryDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);
        dto.setSort(PostQueryDTO.PostSort.HOT);

        PageResult<PostVO> result = postService.getPosts(dto);

        Assertions.assertEquals(1, result.getList().size());
        verify(postMapper).getHotPosts(dto, 11L);
        verify(postMapper, never()).getPosts(dto, 11L);
    }

    @Test
    void getPostsKeepsDefaultMapperQueryForMissingSort() {
        PostVO postVO = new PostVO();
        postVO.setId(2L);
        postVO.setContent("recent post");
        when(postMapper.getPosts(any(PostQueryDTO.class), eq(11L))).thenReturn(List.of(postVO));

        PostQueryDTO dto = new PostQueryDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);

        PageResult<PostVO> result = postService.getPosts(dto);

        Assertions.assertEquals(1, result.getList().size());
        verify(postMapper).getPosts(dto, 11L);
        verify(postMapper, never()).getHotPosts(dto, 11L);
    }

    @Test
    void getPostsAllowsAnonymousHotSortQuery() {
        UserContext.removeUserId();

        PostVO postVO = new PostVO();
        postVO.setId(3L);
        postVO.setContent("anonymous hot");
        when(postMapper.getHotPosts(any(PostQueryDTO.class), isNull())).thenReturn(List.of(postVO));

        PostQueryDTO dto = new PostQueryDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);
        dto.setSort(PostQueryDTO.PostSort.HOT);

        PageResult<PostVO> result = postService.getPosts(dto);

        Assertions.assertEquals(1, result.getList().size());
        verify(postMapper).getHotPosts(dto, null);
    }

    @Test
    void searchEmptyPageKeepsMatchingTotalAndRequestedPageMetadata() {
        Page<PostVO> emptyPage = new Page<>(2, 3);
        emptyPage.setTotal(1);
        when(postMapper.getPosts(any(PostQueryDTO.class), eq(11L))).thenReturn(emptyPage);

        PostQueryDTO dto = new PostQueryDTO();
        dto.setKeyword("Vue");
        dto.setPageNum(2);
        dto.setPageSize(3);
        dto.setSort(PostQueryDTO.PostSort.RECENT);

        PageResult<PostVO> result = postService.getPosts(dto);

        Assertions.assertTrue(result.getList().isEmpty());
        Assertions.assertEquals(1L, result.getTotal());
        Assertions.assertEquals(2, result.getPageNum());
        Assertions.assertEquals(3, result.getPageSize());
    }
}
