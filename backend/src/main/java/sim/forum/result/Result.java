package sim.forum.result;

import lombok.Data;
import java.io.Serializable;

/**
 * 全局统一响应体
 * @param <T> 响应数据的类型
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;    // 状态码 (例如: 200成功, 400业务异常, 500系统崩溃)
    private String msg;      // 提示信息
    private T data;          // 具体的数据内容

    // 私有化构造器，强制使用静态方法构建
    private Result() {}

    /**
     * 成功返回 - 带数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功返回 - 不带数据
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败返回 - 自定义错误码和消息
     */
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    /**
     * 失败返回 - 默认500错误
     */
    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }
}