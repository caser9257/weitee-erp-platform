package cn.weitee.erp.module.system.controller.admin.dept.vo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 部门导入 Response VO")
@Data
@Builder
public class DeptImportRespVO {

    @Schema(description = "创建成功的部门名称数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> createDeptNames;

    @Schema(description = "更新成功的部门名称数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> updateDeptNames;

    @Schema(description = "导入失败的部门集合，key 为部门名称，value 为失败原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> failureDeptNames;

}
