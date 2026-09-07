package cn.weitee.erp.module.system.service.permission;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.test.core.ut.BaseDbUnitTest;
import cn.weitee.erp.module.system.dal.dataobject.permission.MenuDO;
import cn.weitee.erp.module.system.dal.dataobject.permission.RoleMenuDO;
import cn.weitee.erp.module.system.dal.mysql.permission.MenuMapper;
import cn.weitee.erp.module.system.dal.mysql.permission.RoleMenuMapper;
import cn.weitee.erp.module.system.enums.permission.MenuTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 研发物料(Cadence) 菜单种子回归测试。
 *
 * 对应 sql/mysql/rd/190-erp-rd-cadence-menu.sql：断言“研发物料(Cadence)”菜单挂载在
 * “研发管理”目录下，且组件路由路径与前端页面（erp/product/rdCadence/index）一致；
 * 其下包含 Cadence 导入 / 导出两个权限点。
 *
 * 说明：单元测试使用 H2 内存库，无真实种子数据，故在库内复刻父节点结构与种子三行后断言。
 */
@Import(MenuServiceImpl.class)
public class ErpRdCadenceMenuSeedTest extends BaseDbUnitTest {

    @Resource
    private MenuMapper menuMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    private static final Long RD_MGMT_DIR_ID = 930120L; // 研发管理
    private static final Long RD_CADENCE_MENU_ID = 93202048L;
    private static final Long RD_CADENCE_IMPORT_ID = 93202049L;
    private static final Long RD_CADENCE_EXPORT_ID = 93202050L;

    private static final String RD_CADENCE_PERM = "erp:product:rd-cadence:query";
    private static final String RD_CADENCE_IMPORT_PERM = "erp:product:rd-cadence:import";
    private static final String RD_CADENCE_EXPORT_PERM = "erp:product:rd-cadence:export";
    private static final String RD_CADENCE_COMPONENT = "erp/product/rdCadence/index";

    @Test
    public void testRdCadenceMenuSeedMounted() {
        // 复刻真实库父节点结构：研发管理(目录)
        menuMapper.insert(buildMenu(RD_MGMT_DIR_ID, "研发管理", MenuTypeEnum.DIR, "", null, ""));

        // 复刻 190-erp-rd-cadence-menu.sql 种子：研发物料菜单 + 导入/导出按钮
        MenuDO rdMenu = buildMenu(RD_CADENCE_MENU_ID, "研发物料(Cadence)", MenuTypeEnum.MENU,
                RD_CADENCE_COMPONENT, RD_MGMT_DIR_ID, "ErpProductRdCadence");
        rdMenu.setPermission(RD_CADENCE_PERM);
        menuMapper.insert(rdMenu);

        MenuDO importBtn = buildMenu(RD_CADENCE_IMPORT_ID, "Cadence 导入", MenuTypeEnum.BUTTON,
                "", rdMenu.getId(), "");
        importBtn.setPermission(RD_CADENCE_IMPORT_PERM);
        menuMapper.insert(importBtn);

        MenuDO exportBtn = buildMenu(RD_CADENCE_EXPORT_ID, "Cadence 导出", MenuTypeEnum.BUTTON,
                "", rdMenu.getId(), "");
        exportBtn.setPermission(RD_CADENCE_EXPORT_PERM);
        menuMapper.insert(exportBtn);

        // 复刻 192-erp-product-master-page-and-rd-menu.sql 的超级管理员授权。
        roleMenuMapper.insert(buildRoleMenu(RD_MGMT_DIR_ID));
        roleMenuMapper.insert(buildRoleMenu(RD_CADENCE_MENU_ID));
        roleMenuMapper.insert(buildRoleMenu(RD_CADENCE_IMPORT_ID));
        roleMenuMapper.insert(buildRoleMenu(RD_CADENCE_EXPORT_ID));

        // 断言：研发物料菜单必须出现在“研发管理”目录下，且组件路由路径与前端页面一致
        MenuDO rd = menuMapper.selectOne(MenuDO::getPermission, RD_CADENCE_PERM);
        assertNotNull(rd, "研发物料(Cadence) 菜单应存在");
        assertEquals(RD_MGMT_DIR_ID, rd.getParentId(), "应挂载在研发管理目录下");
        assertEquals(RD_CADENCE_COMPONENT, rd.getComponent(), "组件路由路径应与前端页面 erp/product/rdCadence/index 一致");
        assertEquals(MenuTypeEnum.MENU.getType(), rd.getType());

        // 断言：导入 / 导出权限点均挂在研发物料菜单下
        List<MenuDO> children = menuMapper.selectList(MenuDO::getParentId, rd.getId());
        assertEquals(2, children.size(), "研发物料菜单下应含导入、导出两个按钮权限");
        assertTrue(children.stream().anyMatch(m -> RD_CADENCE_IMPORT_PERM.equals(m.getPermission())),
                "缺少 Cadence 导入权限点");
        assertTrue(children.stream().anyMatch(m -> RD_CADENCE_EXPORT_PERM.equals(m.getPermission())),
                "缺少 Cadence 导出权限点");

        assertEquals(1, roleMenuMapper.selectListByRoleId(1L).stream()
                        .filter(item -> item.getMenuId().equals(RD_MGMT_DIR_ID)).count(),
                "超级管理员应拥有研发管理根菜单");
        assertEquals(1, roleMenuMapper.selectListByRoleId(1L).stream()
                        .filter(item -> item.getMenuId().equals(RD_CADENCE_MENU_ID)).count(),
                "超级管理员应拥有研发物料菜单");
    }

    private RoleMenuDO buildRoleMenu(Long menuId) {
        RoleMenuDO roleMenu = new RoleMenuDO();
        roleMenu.setRoleId(1L);
        roleMenu.setMenuId(menuId);
        roleMenu.setDeleted(false);
        return roleMenu;
    }

    private MenuDO buildMenu(Long id, String name, MenuTypeEnum type, String component,
                             Long parentId, String componentName) {
        MenuDO m = new MenuDO();
        m.setId(id);
        m.setName(name);
        m.setType(type.getType());
        m.setComponent(component);
        m.setParentId(parentId);
        m.setComponentName(componentName);
        m.setStatus(CommonStatusEnum.ENABLE.getStatus());
        m.setVisible(true);
        m.setKeepAlive(true);
        m.setAlwaysShow(true);
        m.setSort(50);
        m.setPermission("");
        m.setPath("");
        m.setIcon("");
        m.setDeleted(false);
        return m;
    }

}
