package sim.forum.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class FileUploadService {

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
}