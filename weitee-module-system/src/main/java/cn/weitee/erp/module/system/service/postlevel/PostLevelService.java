package cn.weitee.erp.module.system.service.postlevel;

import cn.weitee.erp.module.system.controller.admin.postlevel.vo.PostLevelAssignUsersReqVO;
import cn.weitee.erp.module.system.controller.admin.postlevel.vo.PostLevelDashboardRespVO;
import cn.weitee.erp.module.system.controller.admin.postlevel.vo.PostLevelDetailRespVO;
import cn.weitee.erp.module.system.controller.admin.postlevel.vo.PostLevelTreeNodeRespVO;
import cn.weitee.erp.module.system.controller.admin.postlevel.vo.PostOrgTreeRespVO;

import java.util.List;

public interface PostLevelService {

    PostLevelDashboardRespVO getDashboard(Long deptId);

    List<PostLevelTreeNodeRespVO> getTree(Long deptId);

    PostOrgTreeRespVO getOrgTree(Long deptId);

    PostLevelDetailRespVO getDetail(Long postId);

    void assignUsers(PostLevelAssignUsersReqVO reqVO);

}