-- Add six Xiaomi-mall products with official images (learning demo)
SET NAMES utf8mb4;

INSERT INTO product (category_id, name, subtitle, main_image, detail, status)
SELECT 1, 'REDMI Buds 8', '50dB 深度降噪｜脂白 / 暮黑 / 萃青｜长续航', '/api/uploads/products/50-buds8-main.png', '{"version":1,"gallery":["/api/uploads/products/50-buds8-main.png","/api/uploads/products/50-buds8-g1.png","/api/uploads/products/50-buds8-g2.jpg","/api/uploads/products/50-buds8-g3.jpg","/api/uploads/products/50-buds8-g4.jpg","/api/uploads/products/50-buds8-g5.jpg","/api/uploads/products/50-buds8-g6.jpg"],"highlights":["50dB 智能降噪","三色可选","小巧舒适入耳","适合通勤与轻度运动"],"paragraphs":["REDMI Buds 8 面向日常降噪场景，机身小巧，通勤路上也能把环境声压下去。","提供脂白、暮黑、萃青三种配色，方便和不同设备搭配。","【学习用】商品信息与图片参考自小米商城公开页面（product_id=23966），仅供学习与功能演示，非官方销售。"],"detailImages":["/api/uploads/products/50-buds8-g1.png","/api/uploads/products/50-buds8-g2.jpg","/api/uploads/products/50-buds8-g3.jpg","/api/uploads/products/50-buds8-g4.jpg"],"specs":[{"label":"形态","value":"入耳式真无线"},{"label":"降噪","value":"最高约 50dB 智能降噪"},{"label":"配色","value":"脂白 / 暮黑 / 萃青"},{"label":"用途","value":"学习演示"}],"colorGalleries":{"脂白":["/api/uploads/products/50-buds8-white.png","/api/uploads/products/50-buds8-g1.png","/api/uploads/products/50-buds8-g2.jpg","/api/uploads/products/50-buds8-g3.jpg"],"暮黑":["/api/uploads/products/50-buds8-black.png","/api/uploads/products/50-buds8-g1.png","/api/uploads/products/50-buds8-g2.jpg","/api/uploads/products/50-buds8-g3.jpg"],"萃青":["/api/uploads/products/50-buds8-green.png","/api/uploads/products/50-buds8-g1.png","/api/uploads/products/50-buds8-g2.jpg","/api/uploads/products/50-buds8-g3.jpg"]},"defaultColor":"脂白","learningNote":"学习用 · 参考自小米商城公开页","sourceNote":"来源：https://www.mi.com/shop/buy/detail?product_id=23966"}', 'ON_SALE'
WHERE NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'REDMI Buds 8');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'BUDS8-REDMI-BUDS-8', '{"颜色": "脂白"}', 229.00, 99, 'ENABLED'
FROM product p WHERE p.name = 'REDMI Buds 8'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'BUDS8-REDMI-BUDS-8');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'BUDS8-REDMI-BUDS-8', '{"颜色": "暮黑"}', 229.00, 99, 'ENABLED'
FROM product p WHERE p.name = 'REDMI Buds 8'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'BUDS8-REDMI-BUDS-8');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'BUDS8-REDMI-BUDS-8', '{"颜色": "萃青"}', 229.00, 99, 'ENABLED'
FROM product p WHERE p.name = 'REDMI Buds 8'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'BUDS8-REDMI-BUDS-8');

INSERT INTO product (category_id, name, subtitle, main_image, detail, status)
SELECT 2, 'REDMI K Pad 2', '高性能平板｜深邃黑 / 太空银｜多内存版本', '/api/uploads/products/51-kpad2-main.png', '{"version":1,"gallery":["/api/uploads/products/51-kpad2-main.png","/api/uploads/products/51-kpad2-g1.jpg","/api/uploads/products/51-kpad2-g2.png","/api/uploads/products/51-kpad2-g3.png","/api/uploads/products/51-kpad2-g4.png","/api/uploads/products/51-kpad2-g5.jpg","/api/uploads/products/51-kpad2-g6.png"],"highlights":["高性能平板定位","多内存与存储组合","深邃黑 / 太空银","适合学习、娱乐与轻办公"],"paragraphs":["REDMI K Pad 2 面向高刷娱乐与轻创作，提供多种内存和存储组合。","深邃黑、太空银两色，按容量选择即可。","【学习用】商品信息与图片参考自小米商城公开页面（product_id=23974），仅供学习与功能演示，非官方销售。"],"detailImages":["/api/uploads/products/51-kpad2-g1.jpg","/api/uploads/products/51-kpad2-g2.png","/api/uploads/products/51-kpad2-g3.png","/api/uploads/products/51-kpad2-g4.png"],"specs":[{"label":"形态","value":"平板电脑"},{"label":"配色","value":"深邃黑 / 太空银"},{"label":"存储","value":"多种内存 + 存储版本"},{"label":"用途","value":"学习演示"}],"colorGalleries":{"深邃黑":["/api/uploads/products/51-kpad2-black.png","/api/uploads/products/51-kpad2-g1.jpg","/api/uploads/products/51-kpad2-g2.png","/api/uploads/products/51-kpad2-g3.png"],"太空银":["/api/uploads/products/51-kpad2-silver.png","/api/uploads/products/51-kpad2-g1.jpg","/api/uploads/products/51-kpad2-g2.png","/api/uploads/products/51-kpad2-g3.png"]},"defaultColor":"深邃黑","learningNote":"学习用 · 参考自小米商城公开页","sourceNote":"来源：https://www.mi.com/shop/buy/detail?product_id=23974"}', 'ON_SALE'
WHERE NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'REDMI K Pad 2');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'KPAD2-REDMI-K-PAD-2-8GB-256GB', '{"颜色": "深邃黑", "版本": "8GB内存 256GB存储"}', 3599.00, 99, 'ENABLED'
FROM product p WHERE p.name = 'REDMI K Pad 2'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'KPAD2-REDMI-K-PAD-2-8GB-256GB');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'KPAD2-REDMI-K-PAD-2-12GB-256GB', '{"颜色": "深邃黑", "版本": "12GB内存 256GB存储"}', 3999.00, 99, 'ENABLED'
FROM product p WHERE p.name = 'REDMI K Pad 2'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'KPAD2-REDMI-K-PAD-2-12GB-256GB');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'KPAD2-REDMI-K-PAD-2-16GB-256GB', '{"颜色": "深邃黑", "版本": "16GB内存 256GB存储"}', 4399.00, 99, 'ENABLED'
FROM product p WHERE p.name = 'REDMI K Pad 2'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'KPAD2-REDMI-K-PAD-2-16GB-256GB');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'KPAD2-REDMI-K-PAD-2-16GB-512GB', '{"颜色": "深邃黑", "版本": "16GB内存 512GB存储"}', 4999.00, 99, 'ENABLED'
FROM product p WHERE p.name = 'REDMI K Pad 2'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'KPAD2-REDMI-K-PAD-2-16GB-512GB');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'KPAD2-REDMI-K-PAD-2-12GB-256GB', '{"颜色": "太空银", "版本": "12GB内存 256GB存储"}', 3999.00, 99, 'ENABLED'
FROM product p WHERE p.name = 'REDMI K Pad 2'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'KPAD2-REDMI-K-PAD-2-12GB-256GB');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'KPAD2-REDMI-K-PAD-2-16GB-256GB', '{"颜色": "太空银", "版本": "16GB内存 256GB存储"}', 4399.00, 99, 'ENABLED'
FROM product p WHERE p.name = 'REDMI K Pad 2'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'KPAD2-REDMI-K-PAD-2-16GB-256GB');

INSERT INTO product (category_id, name, subtitle, main_image, detail, status)
SELECT 2, 'REDMI 显示器 G27U 2026', '27 英寸 4K 电竞显示器｜高刷高色域', '/api/uploads/products/52-g27u-main.jpg', '{"version":1,"gallery":["/api/uploads/products/52-g27u-main.jpg","/api/uploads/products/52-g27u-g1.jpg","/api/uploads/products/52-g27u-g2.jpg","/api/uploads/products/52-g27u-g3.jpg","/api/uploads/products/52-g27u-g4.jpg","/api/uploads/products/52-g27u-g5.jpg","/api/uploads/products/52-g27u-g6.jpg"],"highlights":["27 英寸 4K 显示","电竞向刷新与响应","适合桌面创作与游戏","小米商城在售型号"],"paragraphs":["REDMI 显示器 G27U 2026 把 27 英寸 4K 用在日常桌面，兼顾清晰显示和游戏刷新。","适合接笔记本或主机，作为第二块工作屏或主力屏。","【学习用】商品信息与图片参考自小米商城公开页面（product_id=24087），仅供学习与功能演示，非官方销售。"],"detailImages":["/api/uploads/products/52-g27u-d01.jpg","/api/uploads/products/52-g27u-d02.jpg","/api/uploads/products/52-g27u-d03.jpg","/api/uploads/products/52-g27u-d04.jpg","/api/uploads/products/52-g27u-d05.jpg","/api/uploads/products/52-g27u-d06.jpg","/api/uploads/products/52-g27u-d07.jpg","/api/uploads/products/52-g27u-d08.jpg"],"specs":[{"label":"尺寸","value":"27 英寸"},{"label":"定位","value":"4K 电竞显示器"},{"label":"用途","value":"学习演示"}],"colorGalleries":{"默认":["/api/uploads/products/52-g27u-main.jpg","/api/uploads/products/52-g27u-g1.jpg","/api/uploads/products/52-g27u-g2.jpg","/api/uploads/products/52-g27u-g3.jpg"]},"defaultColor":"默认","learningNote":"学习用 · 参考自小米商城公开页","sourceNote":"来源：https://www.mi.com/shop/buy/detail?product_id=24087"}', 'ON_SALE'
WHERE NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'REDMI 显示器 G27U 2026');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'G27U-REDMI-G27U-2026', '{"颜色": "默认"}', 1399.00, 99, 'ENABLED'
FROM product p WHERE p.name = 'REDMI 显示器 G27U 2026'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'G27U-REDMI-G27U-2026');

INSERT INTO product (category_id, name, subtitle, main_image, detail, status)
SELECT 3, '米家扫拖机器人 4', '扫拖一体｜路径规划｜自动回充', '/api/uploads/products/53-robot4-main.png', '{"version":1,"gallery":["/api/uploads/products/53-robot4-main.png","/api/uploads/products/53-robot4-g1.jpg","/api/uploads/products/53-robot4-g2.jpg","/api/uploads/products/53-robot4-g3.jpg","/api/uploads/products/53-robot4-g4.jpg","/api/uploads/products/53-robot4-g5.jpg","/api/uploads/products/53-robot4-g6.jpg"],"highlights":["扫拖一体","智能路径规划","自动回充续扫","适合日常地面维护"],"paragraphs":["米家扫拖机器人 4 把扫地和拖地放进同一台机器，适合公寓和日常地面浮尘。","支持规划清扫和回充，减少手动来回搬机器。","【学习用】商品信息与图片参考自小米商城公开页面（product_id=23998），仅供学习与功能演示，非官方销售。"],"detailImages":["/api/uploads/products/53-robot4-d01.jpg","/api/uploads/products/53-robot4-d02.jpg","/api/uploads/products/53-robot4-d03.jpg","/api/uploads/products/53-robot4-d04.jpg","/api/uploads/products/53-robot4-d05.jpg","/api/uploads/products/53-robot4-d06.jpg","/api/uploads/products/53-robot4-d07.jpg","/api/uploads/products/53-robot4-d08.jpg"],"specs":[{"label":"形态","value":"扫拖一体机器人"},{"label":"品牌","value":"米家"},{"label":"用途","value":"学习演示"}],"colorGalleries":{"默认":["/api/uploads/products/53-robot4-main.png","/api/uploads/products/53-robot4-g1.jpg","/api/uploads/products/53-robot4-g2.jpg","/api/uploads/products/53-robot4-g3.jpg"]},"defaultColor":"默认","learningNote":"学习用 · 参考自小米商城公开页","sourceNote":"来源：https://www.mi.com/shop/buy/detail?product_id=23998"}', 'ON_SALE'
WHERE NOT EXISTS (SELECT 1 FROM product p WHERE p.name = '米家扫拖机器人 4');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'ROBOT4--4', '{"颜色": "默认"}', 1099.00, 99, 'ENABLED'
FROM product p WHERE p.name = '米家扫拖机器人 4'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'ROBOT4--4');

INSERT INTO product (category_id, name, subtitle, main_image, detail, status)
SELECT 3, '米家空气净化器 6 Max 套装', '大空间净化｜装修滤芯套装｜米家互联', '/api/uploads/products/54-air6max-main.png', '{"version":1,"gallery":["/api/uploads/products/54-air6max-main.png","/api/uploads/products/54-air6max-g1.jpg","/api/uploads/products/54-air6max-g2.jpg","/api/uploads/products/54-air6max-g3.jpg","/api/uploads/products/54-air6max-g4.jpg","/api/uploads/products/54-air6max-g5.jpg","/api/uploads/products/54-air6max-g6.jpg"],"highlights":["大空间净化","套装含滤芯方案","米家 App 互联","适合客厅与装修后换气"],"paragraphs":["米家空气净化器 6 Max 套装面向更大房间，滤芯方案按套装一并给出。","可通过米家 App 查看空气数据和定时。","【学习用】商品信息与图片参考自小米商城公开页面（product_id=23999），仅供学习与功能演示，非官方销售。"],"detailImages":["/api/uploads/products/54-air6max-d01.jpg","/api/uploads/products/54-air6max-d02.jpg","/api/uploads/products/54-air6max-d03.jpg","/api/uploads/products/54-air6max-d04.jpg","/api/uploads/products/54-air6max-d05.jpg","/api/uploads/products/54-air6max-d06.jpg","/api/uploads/products/54-air6max-d07.jpg","/api/uploads/products/54-air6max-d08.jpg"],"specs":[{"label":"形态","value":"空气净化器套装"},{"label":"品牌","value":"米家"},{"label":"用途","value":"学习演示"}],"colorGalleries":{"默认":["/api/uploads/products/54-air6max-main.png","/api/uploads/products/54-air6max-g1.jpg","/api/uploads/products/54-air6max-g2.jpg","/api/uploads/products/54-air6max-g3.jpg"]},"defaultColor":"默认","learningNote":"学习用 · 参考自小米商城公开页","sourceNote":"来源：https://www.mi.com/shop/buy/detail?product_id=23999"}', 'ON_SALE'
WHERE NOT EXISTS (SELECT 1 FROM product p WHERE p.name = '米家空气净化器 6 Max 套装');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'AIR6MAX--6-MAX', '{"颜色": "默认"}', 5898.00, 99, 'ENABLED'
FROM product p WHERE p.name = '米家空气净化器 6 Max 套装'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'AIR6MAX--6-MAX');

INSERT INTO product (category_id, name, subtitle, main_image, detail, status)
SELECT 3, '米家电动剃须刀 Pro 套装版', '往复式剃须｜套装版｜锖色', '/api/uploads/products/55-shaverpro-main.png', '{"version":1,"gallery":["/api/uploads/products/55-shaverpro-main.png","/api/uploads/products/55-shaverpro-g1.jpg","/api/uploads/products/55-shaverpro-g2.jpg","/api/uploads/products/55-shaverpro-g3.jpg","/api/uploads/products/55-shaverpro-g4.jpg","/api/uploads/products/55-shaverpro-g5.jpg","/api/uploads/products/55-shaverpro-g6.jpg"],"highlights":["往复式刀头","套装版配件更全","锖色机身","适合日常剃须"],"paragraphs":["米家电动剃须刀 Pro 套装版把主机和常用配件放在一起，减少单独加购。","机身是锖色，适合放在洗手台或出差收纳。","【学习用】商品信息与图片参考自小米商城公开页面（product_id=23979），仅供学习与功能演示，非官方销售。"],"detailImages":["/api/uploads/products/55-shaverpro-d01.jpg","/api/uploads/products/55-shaverpro-d02.jpg","/api/uploads/products/55-shaverpro-d03.jpg","/api/uploads/products/55-shaverpro-d04.jpg","/api/uploads/products/55-shaverpro-d05.jpg","/api/uploads/products/55-shaverpro-d06.jpg","/api/uploads/products/55-shaverpro-d07.jpg","/api/uploads/products/55-shaverpro-d08.jpg"],"specs":[{"label":"形态","value":"电动剃须刀套装"},{"label":"颜色","value":"锖色"},{"label":"用途","value":"学习演示"}],"colorGalleries":{"锖色":["/api/uploads/products/55-shaverpro-main.png","/api/uploads/products/55-shaverpro-g1.jpg","/api/uploads/products/55-shaverpro-g2.jpg","/api/uploads/products/55-shaverpro-g3.jpg"]},"defaultColor":"锖色","learningNote":"学习用 · 参考自小米商城公开页","sourceNote":"来源：https://www.mi.com/shop/buy/detail?product_id=23979"}', 'ON_SALE'
WHERE NOT EXISTS (SELECT 1 FROM product p WHERE p.name = '米家电动剃须刀 Pro 套装版');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'SHAVERPRO--PRO', '{"颜色": "锖色"}', 594.00, 99, 'ENABLED'
FROM product p WHERE p.name = '米家电动剃须刀 Pro 套装版'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'SHAVERPRO--PRO');
