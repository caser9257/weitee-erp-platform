package cn.weitee.erp.module.mes.dal.dataobject;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

/** MES SOP 文档。 */
@TableName("mes_sop_document")
@KeySequence("mes_sop_document_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesSopDocumentDO extends BaseDO {

    @TableId
    private Long id;
    private String sopNo;
    private String title;
    private String version;
    private String content;
    private String attachmentUrl;
    private Integer status;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
    private String remark;
}
