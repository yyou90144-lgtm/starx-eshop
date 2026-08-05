INSERT INTO category (name, sort_order, status) VALUES
('手机数码', 10, 'ENABLED'),
('电脑办公', 20, 'ENABLED'),
('家居生活', 30, 'ENABLED');

INSERT INTO product (category_id, name, subtitle, main_image, detail, status) VALUES
(1, '智能手机 A1', '高清屏幕与全天续航', '/api/demo/phone.svg', '适合日常通讯、拍照与影音娱乐。', 'ON_SALE'),
(1, '摄影手机 Pro', '大底主摄与光学防抖', '/api/demo/phone.svg', '面向摄影爱好者的影像旗舰手机。', 'ON_SALE'),
(1, '轻薄手机 Mini', '轻巧机身，单手舒适', '/api/demo/phone.svg', '小尺寸轻薄设计，兼顾性能与手感。', 'ON_SALE'),
(1, '旗舰手机 Ultra', '旗舰性能与高亮屏幕', '/api/demo/phone.svg', '高性能处理器与高刷新率显示屏。', 'ON_SALE'),
(1, '长续航手机 Max', '大容量电池与快速充电', '/api/demo/phone.svg', '适合差旅和重度使用场景。', 'ON_SALE'),
(1, '5G 手机青春版', '流畅 5G，年轻配色', '/api/demo/phone.svg', '高性价比 5G 入门手机。', 'ON_SALE'),
(2, '轻薄笔记本 Air', '便携办公，全天续航', '/api/demo/computer.svg', '适合学习、会议和移动办公。', 'ON_SALE'),
(2, '游戏笔记本 G1', '独立显卡与高刷屏', '/api/demo/computer.svg', '适合大型游戏与内容创作。', 'ON_SALE'),
(2, '平板电脑 Pad', '学习娱乐二合一', '/api/demo/computer.svg', '支持影音、网课与轻办公。', 'ON_SALE'),
(2, '27 英寸显示器', '高色域与护眼模式', '/api/demo/computer.svg', '适合办公设计与家庭娱乐。', 'ON_SALE'),
(2, '机械键盘 K87', '热插拔轴体与背光', '/api/demo/computer.svg', '紧凑布局机械键盘。', 'ON_SALE'),
(2, '无线鼠标 M2', '静音按键与多设备', '/api/demo/computer.svg', '适合办公室和移动使用。', 'ON_SALE'),
(3, '智能台灯', '无频闪照明与触控调光', '/api/demo/home.svg', '适合阅读、学习和床头照明。', 'ON_SALE'),
(3, '空气净化器', '高效过滤与空气监测', '/api/demo/home.svg', '改善卧室和客厅空气质量。', 'ON_SALE'),
(3, '无线吸尘器', '轻量机身与强劲吸力', '/api/demo/home.svg', '适合地面、沙发和车内清洁。', 'ON_SALE'),
(3, '智能电饭煲', '多种菜单与预约烹饪', '/api/demo/home.svg', '满足家庭日常煮饭和煲汤。', 'ON_SALE'),
(3, '恒温电水壶', '多段控温与保温', '/api/demo/home.svg', '适合泡茶、咖啡和冲调饮品。', 'ON_SALE'),
(3, '蓝牙音箱', '立体声音效与长续航', '/api/demo/home.svg', '适合居家、露营和聚会。', 'ON_SALE');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status) VALUES
(1, 'PREVIEW-PHONE-001', '{"颜色":"深空黑","容量":"128GB"}', 1999.00, 80, 'ENABLED'),
(2, 'PREVIEW-PHONE-002', '{"颜色":"星河银","容量":"256GB"}', 3299.00, 60, 'ENABLED'),
(3, 'PREVIEW-PHONE-003', '{"颜色":"云朵白","容量":"128GB"}', 1599.00, 90, 'ENABLED'),
(4, 'PREVIEW-PHONE-004', '{"颜色":"曜石黑","容量":"512GB"}', 4999.00, 40, 'ENABLED'),
(5, 'PREVIEW-PHONE-005', '{"颜色":"森林绿","容量":"256GB"}', 2399.00, 70, 'ENABLED'),
(6, 'PREVIEW-PHONE-006', '{"颜色":"晴空蓝","容量":"128GB"}', 1299.00, 120, 'ENABLED'),
(7, 'PREVIEW-PC-001', '{"内存":"16GB","硬盘":"512GB"}', 4599.00, 45, 'ENABLED'),
(8, 'PREVIEW-PC-002', '{"内存":"32GB","硬盘":"1TB"}', 7999.00, 30, 'ENABLED'),
(9, 'PREVIEW-PC-003', '{"颜色":"银色","容量":"256GB"}', 2699.00, 65, 'ENABLED'),
(10, 'PREVIEW-PC-004', '{"分辨率":"2K","刷新率":"165Hz"}', 1499.00, 75, 'ENABLED'),
(11, 'PREVIEW-PC-005', '{"轴体":"线性轴","配色":"深空灰"}', 399.00, 110, 'ENABLED'),
(12, 'PREVIEW-PC-006', '{"颜色":"黑色","连接":"双模"}', 159.00, 150, 'ENABLED'),
(13, 'PREVIEW-HOME-001', '{"颜色":"白色","功率":"12W"}', 199.00, 120, 'ENABLED'),
(14, 'PREVIEW-HOME-002', '{"适用面积":"45㎡","颜色":"白色"}', 1299.00, 50, 'ENABLED'),
(15, 'PREVIEW-HOME-003', '{"颜色":"灰色","续航":"60分钟"}', 1599.00, 45, 'ENABLED'),
(16, 'PREVIEW-HOME-004', '{"容量":"4L","颜色":"白色"}', 499.00, 70, 'ENABLED'),
(17, 'PREVIEW-HOME-005', '{"容量":"1.5L","颜色":"米白"}', 229.00, 100, 'ENABLED'),
(18, 'PREVIEW-HOME-006', '{"颜色":"墨绿色","续航":"12小时"}', 299.00, 90, 'ENABLED');

-- 演示买家与已完成订单：驱动热销榜（按销量统计 PAID/SHIPPED/COMPLETED）
INSERT INTO sys_user (username, password_hash, nickname, phone, role, status) VALUES
('seed_buyer', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsF3/hD2eG1aV5xK.i', '热销种子买家', '13800000001', 'USER', 'ENABLED');

INSERT INTO orders (order_no, user_id, total_amount, status, receiver_name, receiver_phone, receiver_address, paid_at, completed_at, created_at) VALUES
('HOT-SEED-001', 1, 15992.00, 'COMPLETED', '热销买家', '13800000001', '上海市浦东新区演示路 1 号', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('HOT-SEED-002', 1, 9197.00, 'PAID', '热销买家', '13800000001', '上海市浦东新区演示路 1 号', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP),
('HOT-SEED-003', 1, 3897.00, 'SHIPPED', '热销买家', '13800000001', '上海市浦东新区演示路 1 号', CURRENT_TIMESTAMP, NULL, CURRENT_TIMESTAMP),
('HOT-SEED-004', 1, 2596.00, 'COMPLETED', '热销买家', '13800000001', '杭州市西湖区演示路 8 号', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO order_item (order_id, product_id, sku_id, product_name, sku_specs, product_image, price, quantity, subtotal) VALUES
(1, 4, 4, '旗舰手机 Ultra', '{"颜色":"曜石黑","容量":"512GB"}', '/api/demo/phone.svg', 4999.00, 2, 9998.00),
(1, 1, 1, '智能手机 A1', '{"颜色":"深空黑","容量":"128GB"}', '/api/demo/phone.svg', 1999.00, 3, 5997.00),
(2, 8, 8, '游戏笔记本 G1', '{"内存":"32GB","硬盘":"1TB"}', '/api/demo/computer.svg', 7999.00, 1, 7999.00),
(2, 11, 11, '机械键盘 K87', '{"轴体":"线性轴","配色":"深空灰"}', '/api/demo/computer.svg', 399.00, 3, 1197.00),
(3, 2, 2, '摄影手机 Pro', '{"颜色":"星河银","容量":"256GB"}', '/api/demo/phone.svg', 3299.00, 1, 3299.00),
(3, 13, 13, '智能台灯', '{"颜色":"白色","功率":"12W"}', '/api/demo/home.svg', 199.00, 3, 597.00),
(3, 14, 14, '空气净化器', '{"适用面积":"45㎡","颜色":"白色"}', '/api/demo/home.svg', 1299.00, 1, 1299.00),
(4, 6, 6, '5G 手机青春版', '{"颜色":"晴空蓝","容量":"128GB"}', '/api/demo/phone.svg', 1299.00, 2, 2598.00);
