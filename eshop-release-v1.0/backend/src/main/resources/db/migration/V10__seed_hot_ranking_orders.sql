-- 演示热销订单：无销量数据时首页热销榜为空；本迁移幂等写入一组有效订单销量。

INSERT INTO sys_user (username, password_hash, nickname, phone, role, status)
SELECT 'seed_buyer',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsF3/hD2eG1aV5xK.i',
       '热销种子买家',
       '13800000001',
       'USER',
       'ENABLED'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'seed_buyer');

INSERT INTO orders (
    order_no, user_id, total_amount, status,
    receiver_name, receiver_phone, receiver_address,
    paid_at, completed_at, created_at
)
SELECT
    seed.order_no,
    (SELECT id FROM sys_user WHERE username = 'seed_buyer' LIMIT 1),
    seed.total_amount,
    seed.status,
    '热销买家',
    '13800000001',
    '上海市浦东新区演示路 1 号',
    NOW(),
    CASE WHEN seed.status = 'COMPLETED' THEN NOW() ELSE NULL END,
    NOW()
FROM (
    SELECT 'HOT-SEED-001' AS order_no, 15992.00 AS total_amount, 'COMPLETED' AS status
    UNION ALL SELECT 'HOT-SEED-002', 9197.00, 'PAID'
    UNION ALL SELECT 'HOT-SEED-003', 3897.00, 'SHIPPED'
    UNION ALL SELECT 'HOT-SEED-004', 2596.00, 'COMPLETED'
) seed
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE order_no = seed.order_no)
  AND EXISTS (SELECT 1 FROM sys_user WHERE username = 'seed_buyer');

INSERT INTO order_item (
    order_id, product_id, sku_id, product_name, sku_specs, product_image, price, quantity, subtotal
)
SELECT
    (SELECT id FROM orders WHERE order_no = seed.order_no LIMIT 1),
    p.id,
    s.id,
    p.name,
    s.specs_json,
    p.main_image,
    s.price,
    seed.quantity,
    (s.price * seed.quantity)
FROM (
    SELECT 'HOT-SEED-001' AS order_no, '旗舰手机 Ultra' AS product_name, 2 AS quantity
    UNION ALL SELECT 'HOT-SEED-001', '智能手机 A1', 3
    UNION ALL SELECT 'HOT-SEED-002', '游戏笔记本 G1', 1
    UNION ALL SELECT 'HOT-SEED-002', '机械键盘 K87', 3
    UNION ALL SELECT 'HOT-SEED-003', '摄影手机 Pro', 1
    UNION ALL SELECT 'HOT-SEED-003', '智能台灯', 3
    UNION ALL SELECT 'HOT-SEED-004', '5G 手机青春版', 2
) seed
INNER JOIN product p ON p.name = seed.product_name OR p.name = CONCAT('演示', seed.product_name) OR p.name = CONCAT('演示 ', seed.product_name)
INNER JOIN product_sku s ON s.product_id = p.id
WHERE EXISTS (SELECT 1 FROM orders WHERE order_no = seed.order_no)
  AND NOT EXISTS (
      SELECT 1
      FROM order_item oi
      INNER JOIN orders o ON o.id = oi.order_id
      WHERE o.order_no = seed.order_no AND oi.product_id = p.id
  );
