package cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.inquality;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 采购入库 IQC 质检单 Response VO")
@Data
@Accessors(chain = true)
public class ErpPurchaseInQualityRespVO {

    @Schema(description = "质检单编号", example = "1")
    private Long id;

    @Schema(description = "质检单号", example = "CGZJ20260410000001")
    private String no;

    @Schema(description = "采购入库编号", example = "1")
    private Long purchaseInId;

    @Schema(description = "采购入库单号", example = "CGRK20260410000001")
    private String purchaseInNo;

    @Schema(description = "质检单状态", example = "20")
    private Integer status;

    @Schema(description = "质检结果", example = "30")
    private Integer result;

    @Schema(description = "当前轮次", example = "1")
    private Integer currentRoundNo;

    @Schema(description = "是否需要复检", example = "false")
    private Boolean recheckRequired;

    @Schema(description = "复检原因", example = "初检发现异常，需要复核")
    private String recheckReason;

    @Schema(description = "复检发起人编号", example = "1")
    private Long recheckApplyUserId;

    @Schema(description = "复检发起人昵称", example = "超级管理员")
    private String recheckApplyUserNickname;

    @Schema(description = "复检发起时间")
    private LocalDateTime recheckApplyTime;

    @Schema(description = "抽检方案编号", example = "1")
    private Long samplingSchemeId;

    @Schema(description = "抽检方案名称", example = "默认全检")
    private String samplingSchemeName;

    @Schema(description = "抽检方案类型", example = "10")
    private Integer samplingSchemeType;

    @Schema(description = "抽检比例", example = "0.1000")
    private BigDecimal samplingRatio;

    @Schema(description = "固定抽检数", example = "10")
    private BigDecimal samplingFixedCount;

    @Schema(description = "最小抽检数", example = "5")
    private BigDecimal minSampleCount;

    @Schema(description = "最大抽检数", example = "20")
    private BigDecimal maxSampleCount;

    @Schema(description = "质检员编号", example = "1")
    private Long checkerUserId;

    @Schema(description = "质检员昵称", example = "超级管理员")
    private String checkerUserNickname;

    @Schema(description = "质检时间")
    private LocalDateTime checkTime;

    @Schema(description = "质检备注", example = "质检完成")
    private String remark;

    @Schema(description = "最终合格总数", example = "98")
    private BigDecimal passCount;

    @Schema(description = "最终不合格总数", example = "2")
    private BigDecimal rejectCount;

    @Schema(description = "指派质检人编号", example = "1")
    private Long assignedCheckerUserId;

    @Schema(description = "指派质检人昵称", example = "质检员A")
    private String assignedCheckerUserNickname;

    @Schema(description = "指派时间")
    private LocalDateTime assignedCheckerTime;

    @Schema(description = "采购入库审批状态", example = "20")
    private Integer purchaseInStatus;

    @Schema(description = "采购入库质检状态", example = "10")
    private Integer qaStatus;

    @Schema(description = "采购入库待入库状态", example = "10")
    private Integer stockInStatus;
    private BigDecimal stockInCount;
    private BigDecimal remainingStockInCount;

    @Schema(description = "来源采购订单号", example = "CGDD20260410000001")
    private String orderNo;

    @Schema(description = "供应商名称", example = "测试供应商A")
    private String supplierName;

    @Schema(description = "质检明细列表")
    private List<Item> items;

    @Schema(description = "轮次记录列表")
    private List<Round> rounds;

    @Schema(description = "不良明细列表")
    private List<Defect> defects;

    @Schema(description = "管理后台 - 采购入库 IQC 明细")
    @Data
    @Accessors(chain = true)
    public static class Item {

        @Schema(description = "质检明细编号", example = "11")
        private Long id;

        @Schema(description = "采购入库明细编号", example = "21")
        private Long purchaseInItemId;

        @Schema(description = "产品编号", example = "31")
        private Long productId;

        @Schema(description = "仓库编号", example = "41")
        private Long warehouseId;

        @Schema(description = "到货数量", example = "100")
        private BigDecimal count;

        @Schema(description = "抽检数量", example = "10")
        private BigDecimal sampleCount;

        @Schema(description = "最终合格数量", example = "98")
        private BigDecimal qaPassCount;

        @Schema(description = "最终不合格数量", example = "2")
        private BigDecimal qaRejectCount;

        @Schema(description = "最终质检结果", example = "20")
        private Integer qaResult;

        @Schema(description = "最终质检备注", example = "复检后部分放行")
        private String qaRemark;

        @Schema(description = "产品名称", example = "测试物料A")
        private String productName;

        @Schema(description = "产品条码", example = "690000000001")
        private String productBarCode;

        @Schema(description = "产品单位", example = "个")
        private String productUnitName;
    }

    @Schema(description = "管理后台 - 采购入库 IQC 轮次记录")
    @Data
    @Accessors(chain = true)
    public static class Round {

        @Schema(description = "轮次记录编号", example = "1")
        private Long id;

        @Schema(description = "质检明细编号", example = "11")
        private Long qualityItemId;

        @Schema(description = "轮次号", example = "1")
        private Integer roundNo;

        @Schema(description = "轮次类型", example = "1")
        private Integer roundType;

        @Schema(description = "抽检数量", example = "10")
        private BigDecimal sampleCount;

        @Schema(description = "本轮合格数量", example = "8")
        private BigDecimal passCount;

        @Schema(description = "本轮不合格数量", example = "2")
        private BigDecimal rejectCount;

        @Schema(description = "本轮结果", example = "20")
        private Integer result;

        @Schema(description = "检验人编号", example = "1")
        private Long checkerUserId;

        @Schema(description = "检验人昵称", example = "超级管理员")
        private String checkerUserNickname;

        @Schema(description = "检验时间")
        private LocalDateTime checkTime;

        @Schema(description = "本轮备注", example = "初检发现异常")
        private String remark;
    }

    @Schema(description = "管理后台 - 采购入库 IQC 不良明细")
    @Data
    @Accessors(chain = true)
    public static class Defect {

        @Schema(description = "不良明细编号", example = "1")
        private Long id;

        @Schema(description = "轮次记录编号", example = "1")
        private Long roundId;

        @Schema(description = "质检明细编号", example = "11")
        private Long qualityItemId;

        @Schema(description = "不良原因编号", example = "1")
        private Long defectReasonId;

        @Schema(description = "不良原因名称", example = "外观不良")
        private String defectReasonName;

        @Schema(description = "不良数量", example = "2")
        private BigDecimal defectCount;

        @Schema(description = "不良备注", example = "存在明显划痕")
        private String defectRemark;
    }

}
