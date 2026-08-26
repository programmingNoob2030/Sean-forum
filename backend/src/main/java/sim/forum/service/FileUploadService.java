package sim.forum.service;

import sim.forum.exception.BusinessException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {
    private static final long MAX_POST_CONTENT_IMAGE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    // 从 application.yml 读取 D:/upload-files/
    @Value("${file.upload-path}")
    private String uploadPath;

    /**
     * 通用上传方法
     * @param file     上传的文件对象
     * @param category 业务分类 (如: "user/avatar", "board/cover")
     * @return 存储在数据库中的相对路径
     */
    public String upload(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        // 1. 构造时间层级：2026/04/29
        String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

        // 2. 构造完整的相对路径 (用于存库)
        // 示例: 2026/04/29/user/avatar/
        String relativePath = datePath + "/" + category + "/";

        // 3. 构造绝对路径 (用于存磁盘)
        // 示例: D:/upload-files/2026/04/29/user/avatar/
        File folder = new File(uploadPath + relativePath);
        if (!folder.exists()) {
            folder.mkdirs(); // 递归创建缺失的日期和分类文件夹
        }

        // 4. 生成不重复的文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        // 5. 执行物理上传
        try {
            File dest = new File(folder, fileName);
            file.transferTo(dest);
        } catch (IOException e) {
            // 这里建议打印日志，方便你在控制台看报错
            e.printStackTrace();
            throw new RuntimeException("文件物理上传失败", e);
        }

        // 6. 返回相对路径供 Controller 存入数据库
        return relativePath + fileName;
    }

    public String uploadPostContentImage(MultipartFile file, Long userId) {
        validatePostContentImage(file, userId);

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException("帖子正文图片上传失败");
        }

        String originalExtension = normalizeExtension(getExtension(file.getOriginalFilename()));
        String detectedExtension = detectImageExtension(bytes);
        if (detectedExtension == null || !detectedExtension.equals(originalExtension)) {
            throw new BusinessException("帖子正文图片格式无效");
        }

        String relativePath = buildPostContentRelativePath(userId);
        Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path folder = root.resolve(relativePath).normalize();
        if (!folder.startsWith(root)) {
            throw new BusinessException("帖子正文图片保存路径非法");
        }

        try {
            Files.createDirectories(folder);
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + detectedExtension;
            Path target = folder.resolve(fileName).normalize();
            if (!target.startsWith(root)) {
                throw new BusinessException("帖子正文图片保存路径非法");
            }
            Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
            return relativePath + fileName;
        } catch (IOException e) {
            throw new BusinessException("帖子正文图片上传失败");
        }
    }

    private void validatePostContentImage(MultipartFile file, Long userId) {
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("帖子正文图片不能为空");
        }
        if (file.getSize() > MAX_POST_CONTENT_IMAGE_SIZE) {
            throw new BusinessException("帖子正文图片不能超过10MB");
        }

        String extension = normalizeExtension(getExtension(file.getOriginalFilename()));
        if (extension == null || !ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new BusinessException("帖子正文图片仅支持jpg、jpeg、png格式");
        }
    }

    private String buildPostContentRelativePath(Long userId) {
        String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        return datePath + "/post/content/" + userId + "/";
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return null;
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
    }

    private String normalizeExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return null;
        }
        String lower = extension.toLowerCase(Locale.ROOT);
        if ("jpeg".equals(lower) || "jpg".equals(lower)) {
            return "jpg";
        }
        if ("png".equals(lower)) {
            return "png";
        }
        return null;
    }

    private String detectImageExtension(byte[] bytes) {
        try (ImageInputStream inputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (inputStream == null) {
                return null;
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
            if (!readers.hasNext()) {
                return null;
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(inputStream, true, true);
                return normalizeExtension(reader.getFormatName());
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            return null;
        }
    }
}
