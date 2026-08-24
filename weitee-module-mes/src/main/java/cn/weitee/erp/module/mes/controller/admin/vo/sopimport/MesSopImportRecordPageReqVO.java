package cn.weitee.erp.module.mes.controller.admin.vo.sopimport;

import cn.weitee.erp.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesSopImportRecordPageReqVO extends PageParam {

    private Integer status;

}
