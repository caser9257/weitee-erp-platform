package cn.iocoder.yudao.module.erp.service.finance.guard;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 外部账只写注解
 * 标注在只能写入外部账数据的方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExternalLedgerWriteOnly {

}
