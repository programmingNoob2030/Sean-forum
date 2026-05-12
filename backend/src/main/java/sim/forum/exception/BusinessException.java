package sim.forum.exception;

import lombok.Getter;

/**
 * 自定义业务异常：专门用来抛出我们预期的业务错误
 */
@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400; // 默认业务异常码
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}