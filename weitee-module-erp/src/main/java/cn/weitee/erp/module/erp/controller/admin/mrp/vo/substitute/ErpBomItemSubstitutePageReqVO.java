package cn.weitee.erp.module.erp.controller.admin.mrp.vo.substitute;

import cn.weitee.erp.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ErpBomItemSubstitutePageReqVO extends PageParam {

    private Long bomId;

    private Long materialId;

    private List<Long> bomItemIds;

    private Long substituteMaterialId;

    private Boolean enableAutoRecommend;

}
