package cn.weitee.erp.module.mes.controller.admin.vo.sop;

import cn.weitee.erp.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesSopDocumentPageReqVO extends PageParam {

    private String sopNo;

    private String title;

    private Integer status;

}
