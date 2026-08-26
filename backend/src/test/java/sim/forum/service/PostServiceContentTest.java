package sim.forum.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import sim.forum.dto.post.CreatePostDTO;
import sim.forum.dto.post.PostQueryDTO;
import sim.forum.dto.post.content.PostContentCodec;
import sim.forum.dto.post.content.PostContentFormat;
import sim.forum.dto.post.content.PostContentNodeType;
import sim.forum.entity.BrowseRecord;
import sim.forum.entity.Post;
import sim.forum.event.post.PostCreateEvent;
import sim.forum.exception.BusinessException;
import sim.forum.mapper.PostMapper;
import sim.forum.result.PageResult;
import sim.forum.service.impl.PostServiceImpl;
import sim.forum.vo.post.PostVO;
import sim.forum.vo.post.RecentPostVO;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceContentTest {
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
    void createPostAcceptsPlainTextAndPreservesRawContent() {
        CreatePostDTO dto = new CreatePostDTO();
        dto.setBoardId(7L);
        dto.setTitle("plain");
        dto.setContent("Hello 😀\nForum");

        Post saved = new Post();
        saved.setId(100L);
        saved.setContent(dto.getContent());
        when(postMapper.selectById(100L)).thenReturn(saved);
        doAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(100L);
            return null;
        }).when(postMapper).insert(any(Post.class));

        Post post = postService.createPost(dto, 11L);

        Assertions.assertEquals(dto.getContent(), post.getContent());
        verify(eventPublisher).publishEvent(any(PostCreateEvent.class));
    }

    @Test
    void createPostRejectsInvalidStructuredContent() {
        CreatePostDTO dto = new CreatePostDTO();
        dto.setBoardId(7L);
        dto.setTitle("blocks");
        dto.setContent("{\"version\":1,\"nodes\":[{\"type\":\"image\",\"path\":\"https://evil.example/a.png\"}]}");

        Assertions.assertThrows(BusinessException.class, () -> postService.createPost(dto, 11L));

        verify(postMapper, never()).insert(any(Post.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createPostRejectsOverlongPlainTextContent() {
        CreatePostDTO dto = new CreatePostDTO();
        dto.setBoardId(7L);
        dto.setTitle("long");
        dto.setContent("a".repeat(20001));

        Assertions.assertThrows(BusinessException.class, () -> postService.createPost(dto, 11L));

        verify(postMapper, never()).insert(any(Post.class));
    }

    @Test
    void getPostByIdEnrichesStructuredContent() {
        String raw = "{\"version\":1,\"nodes\":[{\"type\":\"text\",\"text\":\"Hello 😀\"},{\"type\":\"image\",\"path\":\"2026/08/24/post/content/11/abc-123.png\"},{\"type\":\"text\",\"text\":\"Bye\"}]}";

        PostVO postVO = new PostVO();
        postVO.setId(1L);
        postVO.setContent(raw);
        when(postMapper.getPostWithRatingConditionById(11L, 1L)).thenReturn(postVO);

        PostVO result = postService.getPostById(11L, 1L);

        Assertions.assertEquals(PostContentFormat.BLOCKS, result.getContentFormat());
        Assertions.assertEquals(3, result.getContentNodes().size());
        Assertions.assertEquals(PostContentNodeType.TEXT, result.getContentNodes().get(0).getType());
        Assertions.assertEquals(PostContentNodeType.IMAGE, result.getContentNodes().get(1).getType());
        Assertions.assertEquals("Hello 😀Bye", result.getContentTextPreview());
        Assertions.assertEquals("2026/08/24/post/content/11/abc-123.png", result.getFirstImagePath());
    }

    @Test
    void getPostsEnrichesPlainTextList() {
        PostVO postVO = new PostVO();
        postVO.setId(3L);
        postVO.setContent("Legacy plain text");
        when(postMapper.getPosts(any(PostQueryDTO.class), eq(11L))).thenReturn(List.of(postVO));

        PostQueryDTO dto = new PostQueryDTO();
        dto.setPageNum(1);
        dto.setPageSize(10);
        PageResult<PostVO> result = postService.getPosts(dto);

        Assertions.assertEquals(1, result.getList().size());
        Assertions.assertEquals(PostContentFormat.PLAIN, result.getList().get(0).getContentFormat());
        Assertions.assertEquals("Legacy plain text", result.getList().get(0).getContentTextPreview());
        Assertions.assertNull(result.getList().get(0).getFirstImagePath());
    }

    @Test
    void getRecentPostsFallsBackToPlainTextWhenStructuredContentIsInvalid() {
        RecentPostVO recentPostVO = new RecentPostVO();
        recentPostVO.setId(2L);
        recentPostVO.setContent("{\"version\":1,\"nodes\":[{\"type\":\"image\",\"path\":\"https://evil.example/a.png\"}]}");
        when(postMapper.getRecentPosts(List.of(2L), 11L)).thenReturn(List.of(recentPostVO));

        List<RecentPostVO> result = postService.getRecentPosts(List.of(2L), 11L);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(PostContentFormat.PLAIN, result.get(0).getContentFormat());
        Assertions.assertEquals(recentPostVO.getContent(), result.get(0).getContentTextPreview());
        Assertions.assertNull(result.get(0).getFirstImagePath());
    }
}
