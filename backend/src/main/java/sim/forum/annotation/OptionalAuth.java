package sim.forum.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD) // 定义注解是一个方法级别的
@Retention(RetentionPolicy.RUNTIME) // 生命周期
@Documented // 是否在文件中显示定义的注解
public @interface OptionalAuth {

}
