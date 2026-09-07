package cn.weitee.erp.module.erp.controller.admin.product.vo.product;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.weitee.erp.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.weitee.erp.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - ERP 产品分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpProductPageReqVO extends PageParam {

    @Schema(description = "产品名称", example = "李四")
    private String name;

    @Schema(description = "物料编号", example = "MAT-001")
    private String materialCode;

    @Schema(description = "产品型号", example = "0603 10K 1%")
    private String standard;

    @Schema(description = "品牌/制造商", example = "风华高科")
    private String brandManufacturer;

    @Schema(description = "审批状态：0草稿 10审批中 20已审批 30已驳回 60处理失败", example = "20")
    private Integer auditStatus;

    @Schema(description = "是否 PCB 元器件", example = "true")
    private Boolean pcbComponent;

    @Schema(description = "产品分类编号", example = "11161")
    private Long categoryId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}