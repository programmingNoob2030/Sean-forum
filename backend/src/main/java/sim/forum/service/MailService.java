package sim.forum.service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Async
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送 HTML 格式的验证码邮件
     * 使用 @Async 开启异步，这样发邮件时前端不会卡顿 2-3 秒
     */
    @Async
    public void sendCodeMail(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("【Forum】身份验证");

            // HTML 内容，可以搞漂亮点
            String content = "<h3>您好：</h3>" +
                    "<p>您正在进行邮箱验证，您的验证码为：</p>" +
                    "<h2 style='color: #409EFF;'>" + code + "</h2>" +
                    "<p>验证码有效期为 5 分钟，请勿告知他人。</p>";

            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace(); // 实际项目建议记录日志
        }
    }
}