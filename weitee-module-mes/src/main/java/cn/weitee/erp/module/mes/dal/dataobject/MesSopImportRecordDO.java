package cn.weitee.erp.module.mes.dal.dataobject;

import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/** MES SOP OCR 导入记录。 */
@TableName("mes_sop_import_record")
@KeySequence("mes_sop_import_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesSopImportRecordDO extends BaseDO {

    @TableId
    private Long id;
    private String fileName;
    private String fileUrl;
    private String ocrText;
    private Integer status;
    private Long sopId;
    private String errorMsg;
}
