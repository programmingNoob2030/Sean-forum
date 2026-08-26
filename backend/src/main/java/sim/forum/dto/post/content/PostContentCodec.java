package sim.forum.dto.post.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sim.forum.exception.BusinessException;
import sim.forum.vo.post.content.PostContentNodeVO;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Component
public class PostContentCodec {
    private static final int MAX_POST_TEXT_CODE_POINTS = 20000;
    private static final int STRUCTURED_VERSION = 1;
    private static final Pattern IMAGE_PATH_PATTERN = Pattern.compile(
            "^[0-9]{4}/[0-9]{2}/[0-9]{2}/post/content/[0-9]+/[A-Za-z0-9_-]+\\.(?:png|jpe?g)$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Set<String> TOP_LEVEL_FIELDS = Set.of("version", "nodes");
    private static final Set<String> NODE_FIELDS = Set.of("type", "text", "path");

    private final ObjectMapper objectMapper;

    public PostContentCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PostContentDocument parseForWrite(String rawContent) {
        if (rawContent == null || rawContent.trim().isEmpty()) {
            throw new BusinessException("帖子内容不能为空");
        }

        String trimmed = rawContent.trim();
        if (looksLikeStructuredDocument(trimmed)) {
            return parseStructuredDocument(trimmed);
        }
        if (rawContent.codePointCount(0, rawContent.length()) > MAX_POST_TEXT_CODE_POINTS) {
            throw new BusinessException("帖子内容不能超过20000个字符");
        }
        return buildPlainTextDocument(rawContent);
    }

    public PostContentDocument normalizeForRead(String rawContent) {
        if (rawContent == null || rawContent.isEmpty()) {
            return buildPlainTextDocument(rawContent == null ? "" : rawContent);
        }

        String trimmed = rawContent.trim();
        if (looksLikeStructuredDocument(trimmed)) {
            try {
                return parseStructuredDocument(trimmed);
            } catch (BusinessException e) {
                log.warn("帖子内容解析失败，按纯文本展示: {}", e.getMessage());
            }
        }
        return buildPlainTextDocument(rawContent);
    }

    public List<PostContentNodeVO> toNodeVoList(PostContentDocument document) {
        List<PostContentNodeVO> list = new ArrayList<>();
        if (document == null || document.getNodes() == null) {
            return list;
        }
        for (PostContentNode node : document.getNodes()) {
            PostContentNodeVO vo = new PostContentNodeVO();
            vo.setType(node.getType());
            vo.setText(node.getText());
            vo.setPath(node.getPath());
            list.add(vo);
        }
        return list;
    }

    private boolean looksLikeStructuredDocument(String content) {
        return content != null && content.startsWith("{");
    }

    private PostContentDocument parseStructuredDocument(String rawContent) {
        try {
            JsonNode root = objectMapper.readTree(rawContent);
            if (!root.isObject()) {
                throw new BusinessException("帖子内容格式不受支持");
            }

            validateAllowedFields(root, TOP_LEVEL_FIELDS, "帖子内容格式不受支持");

            JsonNode versionNode = root.get("version");
            if (versionNode == null || !versionNode.isIntegralNumber() || versionNode.asInt() != STRUCTURED_VERSION) {
                throw new BusinessException("帖子内容版本不受支持");
            }

            JsonNode nodesNode = root.get("nodes");
            if (nodesNode == null || !nodesNode.isArray()) {
                throw new BusinessException("帖子内容格式不受支持");
            }

            PostContentDocument document = new PostContentDocument();
            document.setVersion(STRUCTURED_VERSION);
            document.setFormat(PostContentFormat.BLOCKS);

            StringBuilder textPreview = new StringBuilder();
            int textCodePointCount = 0;
            boolean hasImage = false;

            for (JsonNode nodeNode : nodesNode) {
                if (!nodeNode.isObject()) {
                    throw new BusinessException("帖子内容节点格式不受支持");
                }
                validateAllowedFields(nodeNode, NODE_FIELDS, "帖子内容节点格式不受支持");

                JsonNode typeNode = nodeNode.get("type");
                PostContentNodeType type = PostContentNodeType.fromValue(typeNode == null ? null : typeNode.asText());
                if (type == null) {
                    throw new BusinessException("帖子内容节点类型不受支持");
                }

                PostContentNode node = new PostContentNode();
                node.setType(type);

                if (type == PostContentNodeType.TEXT) {
                    JsonNode textNode = nodeNode.get("text");
                    if (textNode == null || textNode.isNull() || !textNode.isTextual()) {
                        throw new BusinessException("帖子内容文本节点格式不受支持");
                    }
                    String text = textNode.asText();
                    node.setText(text);
                    textPreview.append(text);
                    textCodePointCount += text.codePointCount(0, text.length());
                } else if (type == PostContentNodeType.IMAGE) {
                    JsonNode pathNode = nodeNode.get("path");
                    if (pathNode == null || pathNode.isNull() || !pathNode.isTextual()) {
                        throw new BusinessException("帖子内容图片节点格式不受支持");
                    }
                    String path = pathNode.asText();
                    if (!isValidImagePath(path)) {
                        throw new BusinessException("帖子图片路径不合法");
                    }
                    node.setPath(path);
                    hasImage = true;
                    if (document.getFirstImagePath() == null) {
                        document.setFirstImagePath(path);
                    }
                }

                document.getNodes().add(node);
            }

            document.setTextPreview(textPreview.toString());
            document.setTextCodePointCount(textCodePointCount);
            validateDocument(document, hasImage);
            return document;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("帖子内容格式不受支持");
        }
    }

    private void validateDocument(PostContentDocument document, boolean hasImage) {
        if (document.getNodes() == null || document.getNodes().isEmpty()) {
            throw new BusinessException("帖子内容不能为空");
        }

        if (document.getTextCodePointCount() > MAX_POST_TEXT_CODE_POINTS) {
            throw new BusinessException("帖子内容不能超过20000个字符");
        }

        String preview = document.getTextPreview() == null ? "" : document.getTextPreview().strip();
        if (!hasImage && preview.isEmpty()) {
            throw new BusinessException("帖子内容不能为空");
        }
    }

    private PostContentDocument buildPlainTextDocument(String rawContent) {
        String content = rawContent == null ? "" : rawContent;
        PostContentDocument document = new PostContentDocument();
        document.setFormat(PostContentFormat.PLAIN);
        document.setNodes(new ArrayList<>());
        PostContentNode node = new PostContentNode();
        node.setType(PostContentNodeType.TEXT);
        node.setText(content);
        document.getNodes().add(node);
        document.setTextPreview(content);
        document.setTextCodePointCount(content.codePointCount(0, content.length()));
        return document;
    }

    private void validateAllowedFields(JsonNode node, Set<String> allowedFields, String message) {
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (!allowedFields.contains(fieldName)) {
                throw new BusinessException(message);
            }
        }
    }

    private boolean isValidImagePath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        if (path.contains("://") || path.startsWith("/") || path.contains("\\") || path.contains("..")) {
            return false;
        }
        return IMAGE_PATH_PATTERN.matcher(path).matches();
    }
}
