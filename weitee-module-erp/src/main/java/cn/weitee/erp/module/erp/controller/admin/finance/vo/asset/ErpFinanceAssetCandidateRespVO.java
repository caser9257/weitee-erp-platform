package cn.weitee.erp.module.erp.controller.admin.finance.vo.asset;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ErpFinanceAssetCandidateRespVO {

    private Long id;
    private Integer sourceType;
    private Long sourceBizId;
    private String sourceBizNo;
    private String assetName;
    private String categoryName;
    private BigDecimal amount;
    private LocalDate purchaseDate;
    private Long deptId;
    private Long responsibleUserId;
    private Integer status;
    private String remark;
    private String creator;
    private LocalDateTime createTime;
}
