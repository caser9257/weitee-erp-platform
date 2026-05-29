package cn.iocoder.yudao.module.system.service.postlevel;

import cn.iocoder.yudao.module.system.controller.admin.postlevel.vo.PostLevelAssignUsersReqVO;
import cn.iocoder.yudao.module.system.controller.admin.postlevel.vo.PostLevelDashboardRespVO;
import cn.iocoder.yudao.module.system.controller.admin.postlevel.vo.PostLevelDetailRespVO;
import cn.iocoder.yudao.module.system.controller.admin.postlevel.vo.PostLevelTreeNodeRespVO;
import cn.iocoder.yudao.module.system.controller.admin.postlevel.vo.PostOrgTreeRespVO;

import java.util.List;

public interface PostLevelService {

    PostLevelDashboardRespVO getDashboard(Long deptId);

    List<PostLevelTreeNodeRespVO> getTree(Long deptId);

    PostOrgTreeRespVO getOrgTree(Long deptId);

    PostLevelDetailRespVO getDetail(Long postId);

    void assignUsers(PostLevelAssignUsersReqVO reqVO);

}