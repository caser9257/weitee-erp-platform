-- 部门岗位模板导入 SQL（适配 ruoyi-vue-pro / MySQL）
-- 说明：
-- 1. system_dept 维护组织树，研发/制造等多级组织建议落成真实部门树
-- 2. system_post 按“同部门 + 同岗位 = 一条岗位模板”导入
-- 3. 同一部门下重复出现的岗位名称，不再导入多条岗位，而是聚合到 staff_quota（编制人数）
-- 4. 人员任职关系仍通过 system_user_post / system_users.post_ids 维护
-- 閮ㄩ棬銆佸矖浣嶅鍏?SQL锛堥€傞厤 ruoyi-vue-pro / MySQL锛?-- 璇存槑锛?-- 1. system_dept 涓洪儴闂ㄦ爲
-- 2. system_post 涓哄叏灞€宀椾綅琛紝涓嶇洿鎺ヤ笌閮ㄩ棬琛ㄥ缓绔嬪閿叧绯?-- 3. 濡傞渶鎶婂矖浣嶅垎閰嶅埌鍏蜂綋浜哄憳锛岃鍚庣画鏇存柊 system_users.dept_id / post_ids

SET @TENANT_ID = 1;
SET @ROOT_DEPT_ID = 100;

START TRANSACTION;

-- 1. 瀵煎叆椤剁骇閮ㄩ棬
INSERT INTO system_dept
(`name`, `parent_id`, `sort`, `leader_user_id`, `phone`, `email`, `status`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT t.name, @ROOT_DEPT_ID, t.sort, NULL, NULL, NULL, 0,
       'admin', NOW(), 'admin', NOW(), b'0', @TENANT_ID
FROM (
    SELECT '缁熻閮? AS name, 10 AS sort
    UNION ALL SELECT '璐㈠姟閮?, 20
    UNION ALL SELECT '浜哄姏璧勬簮閮?, 30
    UNION ALL SELECT '渚涘簲閾鹃儴', 40
    UNION ALL SELECT '宸ヨ壓閮?, 50
    UNION ALL SELECT '甯傚満钀ラ攢閮?, 60
    UNION ALL SELECT '璐ㄩ噺閮?, 70
    UNION ALL SELECT '涓撳鍔?, 80
    UNION ALL SELECT '鐮斿彂閮?绯荤粺閮?璋冩祴缁?, 90
    UNION ALL SELECT '鐮斿彂閮?绯荤粺閮?娴嬭瘯缁?, 100
    UNION ALL SELECT '鐮斿彂閮?绯荤粺閮?灏勯缁?, 110
    UNION ALL SELECT '鐮斿彂閮?绯荤粺閮?缁撴瀯缁?, 120
    UNION ALL SELECT '鐮斿彂閮?杞欢閮?, 130
    UNION ALL SELECT '鐮斿彂閮?纭欢閮?, 140
    UNION ALL SELECT '鍒堕€犻儴', 150
) t
WHERE NOT EXISTS (
    SELECT 1
    FROM system_dept d
    WHERE d.name = t.name
      AND d.parent_id = @ROOT_DEPT_ID
      AND d.tenant_id = @TENANT_ID
      AND d.deleted = b'0'
);

-- 2. 瀵煎叆鍒堕€犻儴涓嬬骇閮ㄩ棬
SET @MANUFACTURE_DEPT_ID = (
    SELECT id
    FROM system_dept
    WHERE name = '鍒堕€犻儴'
      AND parent_id = @ROOT_DEPT_ID
      AND tenant_id = @TENANT_ID
      AND deleted = b'0'
    LIMIT 1
);

INSERT INTO system_dept
(`name`, `parent_id`, `sort`, `leader_user_id`, `phone`, `email`, `status`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT t.name, @MANUFACTURE_DEPT_ID, t.sort, NULL, NULL, NULL, 0,
       'admin', NOW(), 'admin', NOW(), b'0', @TENANT_ID
FROM (
    SELECT '鍒堕€犻儴-SMT缁? AS name, 10 AS sort
    UNION ALL SELECT '鍒堕€犻儴-鐢佃缁?, 20
    UNION ALL SELECT '鍒堕€犻儴-绮樻帴1缁?, 30
    UNION ALL SELECT '鍒堕€犻儴-绮樻帴2缁?, 40
    UNION ALL SELECT '鍒堕€犻儴-绾ょ剨缁?, 50
    UNION ALL SELECT '鍒堕€犻儴-閿悎缁?, 60
    UNION ALL SELECT '鍒堕€犻儴-婵€鍏夊皝鐒婄粍', 70
) t
WHERE @MANUFACTURE_DEPT_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM system_dept d
      WHERE d.name = t.name
        AND d.parent_id = @MANUFACTURE_DEPT_ID
        AND d.tenant_id = @TENANT_ID
        AND d.deleted = b'0'
  );

-- 3. 瀵煎叆宀椾綅
-- 娉ㄦ剰锛氬矖浣嶈〃鏄叏灞€瀛楀吀锛屽悓鍚嶅矖浣嶄粎淇濈暀涓€鏉?INSERT INTO system_post
(`code`, `name`, `sort`, `status`, `remark`,
 `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
SELECT t.code, t.name, t.sort, 0, t.remark,
       'admin', NOW(), 'admin', NOW(), b'0', @TENANT_ID
FROM (
    SELECT 'post_001' AS code, '椤圭洰鍔╃悊' AS name, 10 AS sort, '鏉ユ簮锛氱粺璁￠儴' AS remark
    UNION ALL SELECT 'post_002', '椤圭洰绠＄悊', 20, '鏉ユ簮锛氱粺璁￠儴'
    UNION ALL SELECT 'post_003', '浼氳', 30, '鏉ユ簮锛氳储鍔￠儴'
    UNION ALL SELECT 'post_004', '鍑虹撼', 40, '鏉ユ簮锛氳储鍔￠儴'
    UNION ALL SELECT 'post_005', 'IT', 50, '鏉ユ簮锛氫汉鍔涜祫婧愰儴'
    UNION ALL SELECT 'post_006', '琛屾斂', 60, '鏉ユ簮锛氫汉鍔涜祫婧愰儴'
    UNION ALL SELECT 'post_007', '鍩硅', 70, '鏉ユ簮锛氫汉鍔涜祫婧愰儴'
    UNION ALL SELECT 'post_008', '鎷涜仒', 80, '鏉ユ簮锛氫汉鍔涜祫婧愰儴'
    UNION ALL SELECT 'post_009', '淇濆瘑涓撳憳', 90, '鏉ユ簮锛氫汉鍔涜祫婧愰儴'
    UNION ALL SELECT 'post_010', '鑺辨牱鍛?, 100, '鏉ユ簮锛氫緵搴旈摼閮?
    UNION ALL SELECT 'post_011', '宸ヨ壓宸ョ▼甯?, 110, '鏉ユ簮锛氬伐鑹洪儴'
    UNION ALL SELECT 'post_012', '瀹㈡埛缁忕悊', 120, '鏉ユ簮锛氬競鍦鸿惀閿€閮?
    UNION ALL SELECT 'post_013', '閿€鍞姪鐞?, 130, '鏉ユ簮锛氬競鍦鸿惀閿€閮?
    UNION ALL SELECT 'post_014', '閿€鍞€荤洃', 140, '鏉ユ簮锛氬競鍦鸿惀閿€閮?
    UNION ALL SELECT 'post_015', '浣撶郴宸ョ▼甯?, 150, '鏉ユ簮锛氬競鍦鸿惀閿€閮?
    UNION ALL SELECT 'post_016', '璐ㄩ噺鍔╃悊', 160, '鏉ユ簮锛氳川閲忛儴'
    UNION ALL SELECT 'post_017', '妫€楠屽憳', 170, '鏉ユ簮锛氳川閲忛儴'
    UNION ALL SELECT 'post_018', '鍞墠鎶€鏈敮鎸?, 180, '鏉ユ簮锛氫笓瀹跺姙'
    UNION ALL SELECT 'post_019', '娴嬭瘯鎶€鏈憳', 190, '鏉ユ簮锛氱爺鍙戦儴-绯荤粺閮?璋冩祴缁?
    UNION ALL SELECT 'post_020', '寰粍瑁呰皟娴嬭瘯宸ョ▼甯?, 200, '鏉ユ簮锛氱爺鍙戦儴-绯荤粺閮?璋冩祴缁?
    UNION ALL SELECT 'post_021', '浜у搧璋冭瘯宸ョ▼甯?, 210, '鏉ユ簮锛氱爺鍙戦儴-绯荤粺閮?娴嬭瘯缁?
    UNION ALL SELECT 'post_022', '杞欢娴嬭瘯宸ョ▼甯?, 220, '鏉ユ簮锛氱爺鍙戦儴-绯荤粺閮?娴嬭瘯缁?
    UNION ALL SELECT 'post_023', '鏁存満娴嬭瘯宸ョ▼甯?, 230, '鏉ユ簮锛氱爺鍙戦儴-绯荤粺閮?娴嬭瘯缁?
    UNION ALL SELECT 'post_024', '灏勯宸ョ▼甯?, 240, '鏉ユ簮锛氱爺鍙戦儴-绯荤粺閮?灏勯缁?
    UNION ALL SELECT 'post_025', '缁撴瀯宸ョ▼甯?, 250, '鏉ユ簮锛氱爺鍙戦儴-绯荤粺閮?缁撴瀯缁?
    UNION ALL SELECT 'post_026', 'C++鐮斿彂宸ョ▼甯?, 260, '鏉ユ簮锛氱爺鍙戦儴-绯荤粺閮?缁撴瀯缁?
    UNION ALL SELECT 'post_027', '宓屽叆寮忓伐绋嬪笀', 270, '鏉ユ簮锛氱爺鍙戦儴-杞欢閮?
    UNION ALL SELECT 'post_028', '宓屽叆寮忓疄涔犵敓', 280, '鏉ユ簮锛氱爺鍙戦儴-杞欢閮?
    UNION ALL SELECT 'post_029', '淇℃伅鍖栧伐绋嬪笀', 290, '鏉ユ簮锛氱爺鍙戦儴-杞欢閮?
    UNION ALL SELECT 'post_030', '鐢熶骇璁″垝鍛?, 300, '鏉ユ簮锛氬埗閫犻儴'
    UNION ALL SELECT 'post_031', '鍚庡嫟鍛?, 310, '鏉ユ簮锛氬埗閫犻儴'
    UNION ALL SELECT 'post_032', '鐗╂祦鍛?, 320, '鏉ユ簮锛氬埗閫犻儴'
) t
WHERE NOT EXISTS (
    SELECT 1
    FROM system_post p
    WHERE p.name = t.name
      AND p.tenant_id = @TENANT_ID
      AND p.deleted = b'0'
);

COMMIT;
