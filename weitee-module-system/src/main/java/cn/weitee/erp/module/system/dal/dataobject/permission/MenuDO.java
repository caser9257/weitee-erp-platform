package cn.weitee.erp.module.system.dal.dataobject.permission;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.mybatis.core.dataobject.BaseDO;
import cn.weitee.erp.module.system.enums.permission.MenuTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单 DO
 *
 * @author weitee
 */
@TableName("system_menu")
@KeySequence("system_menu_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写銆?
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuDO extends BaseDO {

    /**
     * 菜单编号 - 根节鐐?
     */
    public static final Long ID_ROOT = 0L;

    /**
     * 菜单编号
     */
    @TableId
    private Long id;
    /**
     * 菜单名称
     */
    private String name;
    /**
     * 权限标识
     *
     * 一般格式为锛?{系统}:${模块}:${操作}
     * 例如说：system:admin:add，即 system 服务的添加管理员銆?
     *
     * 当我们把璇?MenuDO 赋予给角色后，意味着该角色有该资源：
     * - 对于后端，配鍚?@PreAuthorize 注解，配缃?API 接口需要该权限，从而对 API 接口进行权限控制銆?
     * - 对于前端，配合前端标签，配置按钮是否展示，避免用户没有该权限时，结果可以看到该操作銆?
     */
    private String permission;
    /**
     * 菜单类型
     *
     * 枚举 {@link MenuTypeEnum}
     */
    private Integer type;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 父菜单ID
     */
    private Long parentId;
    /**
     * 路由地址
     *
     * 如果 path 涓?http(s) 时，则它是外閾?
     */
    private String path;
    /**
     * 菜单图标
     */
    private String icon;
    /**
     * 组件路径
     */
    private String component;
    /**
     * 组件鍚?
     */
    private String componentName;
    /**
     * 状鎬?
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 是否可见
     *
     * 只有菜单、目录使鐢?
     * 当设置为 true 时，该菜单不会展示在侧边栏，但是路由还是存在。例如说，一些独立的编辑页面 /edit/1024 等等
     */
    private Boolean visible;
    /**
     * 是否缓存
     *
     * 只有菜单、目录使用，否使鐢?Vue 路由鐨?keep-alive 特鎬?
     * 注意：如果开启缓存，则必须填鍐?{@link #componentName} 属性，否则无法缓存
     */
    private Boolean keepAlive;
    /**
     * 是否总是显示
     *
     * 如果涓?false 时，当该菜单只有一个子菜单时，不展示自己，直接展示子菜鍗?
     */
    private Boolean alwaysShow;

}
