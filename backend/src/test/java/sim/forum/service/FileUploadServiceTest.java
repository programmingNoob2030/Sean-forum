package sim.forum.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import sim.forum.exception.BusinessException;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

class FileUploadServiceTest {
    private FileUploadService fileUploadService;

    @BeforeEach
    void setUp() throws Exception {
        fileUploadService = new FileUploadService();
        Path tempDir = Files.createTempDirectory("post-content-upload-test");
        ReflectionTestUtils.setField(fileUploadService, "uploadPath", tempDir.toAbsolutePath().toString());
        this.tempDir = tempDir;
    }

    private Path tempDir;

    @Test
    void uploadPostContentImageStoresFileUnderUserDirectory() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.jpg",
                "image/jpeg",
                createImageBytes("jpg")
        );

        String path = fileUploadService.uploadPostContentImage(file, 12L);

        Assertions.assertTrue(path.matches("\\d{4}/\\d{2}/\\d{2}/post/content/12/[A-Za-z0-9]+\\.jpg"));
        Assertions.assertTrue(Files.exists(tempDir.resolve(path)));
    }

    @Test
    void uploadPostContentImageRejectsInvalidImageBytes() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.png",
                "image/png",
                "not-an-image".getBytes()
        );

        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> fileUploadService.uploadPostContentImage(file, 12L));

        Assertions.assertEquals("帖子正文图片格式无效", exception.getMessage());
    }

    @Test
    void uploadPostContentImageRejectsOverSizeFile() {
        byte[] bytes = new byte[(int) (10L * 1024 * 1024 + 1)];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.jpg",
                "image/jpeg",
                bytes
        );

        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> fileUploadService.uploadPostContentImage(file, 12L));

        Assertions.assertEquals("帖子正文图片不能超过10MB", exception.getMessage());
    }

    @Test
    void uploadPostContentImageRejectsExtensionMismatch() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.jpg",
                "image/jpeg",
                createImageBytes("png")
        );

        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> fileUploadService.uploadPostContentImage(file, 12L));

        Assertions.assertEquals("帖子正文图片格式无效", exception.getMessage());
    }

    private byte[] createImageBytes(String format) throws Exception {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, Color.WHITE.getRGB());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        return outputStream.toByteArray();
    }
}
