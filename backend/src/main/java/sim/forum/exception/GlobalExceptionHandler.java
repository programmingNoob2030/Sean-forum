package sim.forum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sim.forum.result.Result;

/**
 * 全局异常处理器：拦截并统一封装返回格式
 */
@Slf4j // 自动生成 log 对象，方便我打印报错堆栈
@RestControllerAdvice // 告诉 Spring 这是一个全局的“异常收集站”
public class GlobalExceptionHandler {

    /**
     * 处理自定义的业务异常
     * 场景：用户名重复、密码错误等
     */
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理 JSR-303 参数校验异常
     * 场景：@NotBlank、@Size 校验失败时
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("参数校验失败：{}", msg);
        return Result.error(400, msg);
    }

    /**
     * 最后的兜底：拦截未知的系统异常
     * 场景：数据库断开、空指针、代码 Bug
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统运行异常：", e); // 把详细堆栈打印在日志里，方便你以后修 Bug
        return Result.error(500, "服务器开小差了，请稍后再试");
    }
}