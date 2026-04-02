-- 评论管理菜单和权限配置 SQL
-- 请在系统管理数据库中执行此脚本

-- 1. 插入一级菜单（系统管理下的子菜单）
-- 假设系统管理的 parent_id 为 '1'，请根据实际情况调整
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2000, '评论管理', '1', '10', 'comment', 'system/comment/index', '', 1, 0, 'C', '0', '0', 'system:comment:list', 'comment', 'admin', NOW(), '文章评论管理菜单');

-- 2. 插入按钮权限
-- 评论查询
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2001, '评论查询', '2000', '1', '', '', 1, 0, 'F', '0', '0', 'system:comment:query', '#', 'admin', NOW(), '');

-- 评论新增
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2002, '评论新增', '2000', '2', '', '', 1, 0, 'F', '0', '0', 'system:comment:add', '#', 'admin', NOW(), '');

-- 评论修改
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2003, '评论修改', '2000', '3', '', '', 1, 0, 'F', '0', '0', 'system:comment:edit', '#', 'admin', NOW(), '');

-- 评论删除
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2004, '评论删除', '2000', '4', '', '', 1, 0, 'F', '0', '0', 'system:comment:remove', '#', 'admin', NOW(), '');

-- 评论导出
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2005, '评论导出', '2000', '5', '', '', 1, 0, 'F', '0', '0', 'system:comment:export', '#', 'admin', NOW(), '');

-- 3. 为管理员角色分配权限（假设管理员角色 ID 为 1）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2000);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2001);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2002);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2003);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2004);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 2005);

-- 注释说明：
-- 1. 请根据您的实际数据库结构调整 menu_id 的起始值
-- 2. parent_id 需要根据实际的系统管理菜单 ID 进行调整
-- 3. role_id 需要根据实际的管理员角色 ID 进行调整
-- 4. 执行后需要在系统中刷新菜单缓存
