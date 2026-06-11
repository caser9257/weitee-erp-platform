package cn.iocoder.yudao.module.bpm.dal.dataobject.approval;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 节假日 DO
 *
 * 记录节假日信息，用于审批时判断是否为工作日
 */
@TableName("bpm_approval_holiday")
@KeySequence("bpm_approval_holiday_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmApprovalHolidayDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 节假日日期
     */
    private Date holidayDate;

    /**
     * 节假日名称
     */
    private String holidayName;

    /**
     * 节假日类型
     *
     * NATIONAL: 法定节假日
     * WEEKEND: 周末
     * CUSTOM: 自定义
     */
    private String holidayType;

    /**
     * 是否为工作日（用于调休）
     */
    private Boolean isWorkday;

}
