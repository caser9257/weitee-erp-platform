package cn.weitee.erp.module.system.controller.admin.dept.vo.dept;

import cn.idev.excel.annotation.ExcelProperty;
import cn.weitee.erp.framework.excel.core.annotations.DictFormat;
import cn.weitee.erp.framework.excel.core.convert.DictConvert;
import cn.weitee.erp.module.system.enums.DictTypeConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 部门 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeptImportExcelVO {

    @ExcelProperty("部门名称")
    private String name;

    @ExcelProperty("上级部门名称")
    private String parentName;

    @ExcelProperty("显示顺序")
    private Integer sort;

    @ExcelProperty("负责人账号")
    private String leaderUsername;

    @ExcelProperty("联系电话")
    private String phone;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty(value = "账号状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

}
