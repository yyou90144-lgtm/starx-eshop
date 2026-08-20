-- Add REDMI K90 Max from mi.com product_id=23969
INSERT INTO product (category_id, name, subtitle, main_image, detail, status)
SELECT 1, 'REDMI K90 Max', '天玑 9500 正代旗舰芯｜游戏独显｜超强风冷主动散热｜165Hz 超高刷电竞屏',
       '/api/uploads/products/19-k90max-black.png', '{"version":1,"gallery":["/api/uploads/products/19-k90max-g1.jpg","/api/uploads/products/19-k90max-g2.jpg","/api/uploads/products/19-k90max-g3.jpg","/api/uploads/products/19-k90max-g4.jpg","/api/uploads/products/19-k90max-g5.jpg"],"highlights":["天玑 9500 正代旗舰芯","游戏独显 + 超强风冷主动散热","165Hz 超高刷电竞屏","小米商城同款规格演示"],"paragraphs":["REDMI K90 Max 面向高性能与游戏场景，搭载天玑 9500 旗舰平台，并配备游戏独显与主动风冷散热方案。","6.83 英寸 165Hz 高刷电竞屏，配合大容量电池与高功率快充，长时间游玩与日常重度使用更从容。","提供暗影黑、天际蓝、太空银三色，以及多种存储版本。本页信息参考小米商城 product_id=23969，用于星选商城演示。"],"detailImages":["/api/uploads/products/19-k90max-g1.jpg","/api/uploads/products/19-k90max-g2.jpg","/api/uploads/products/19-k90max-g3.jpg","/api/uploads/products/19-k90max-g4.jpg","/api/uploads/products/19-k90max-g5.jpg"],"specs":[{"label":"平台","value":"天玑 9500"},{"label":"屏幕","value":"6.83 英寸 165Hz 电竞屏"},{"label":"散热","value":"超强风冷主动散热"},{"label":"来源","value":"小米商城 REDMI K90 Max"}],"colorGalleries":{"暗影黑":["/api/uploads/products/19-k90max-black.png","/api/uploads/products/19-k90max-g1.jpg","/api/uploads/products/19-k90max-g2.jpg","/api/uploads/products/19-k90max-g3.jpg"],"天际蓝":["/api/uploads/products/19-k90max-blue.png","/api/uploads/products/19-k90max-g1.jpg","/api/uploads/products/19-k90max-g2.jpg","/api/uploads/products/19-k90max-g3.jpg"],"太空银":["/api/uploads/products/19-k90max-silver.png","/api/uploads/products/19-k90max-g1.jpg","/api/uploads/products/19-k90max-g2.jpg","/api/uploads/products/19-k90max-g3.jpg"]},"defaultColor":"暗影黑"}', 'ON_SALE'
WHERE NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'REDMI K90 Max');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'K90MAX-BLACK-12-256', '{"颜色": "暗影黑", "存储容量": "12GB+256GB"}', 3499.00, 99, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI K90 Max' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'K90MAX-BLACK-12-256');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'K90MAX-BLUE-12-256', '{"颜色": "天际蓝", "存储容量": "12GB+256GB"}', 3499.00, 99, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI K90 Max' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'K90MAX-BLUE-12-256');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'K90MAX-SILVER-12-256', '{"颜色": "太空银", "存储容量": "12GB+256GB"}', 3499.00, 99, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI K90 Max' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'K90MAX-SILVER-12-256');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'K90MAX-BLACK-12-512', '{"颜色": "暗影黑", "存储容量": "12GB+512GB"}', 3999.00, 99, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI K90 Max' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'K90MAX-BLACK-12-512');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'K90MAX-BLUE-12-512', '{"颜色": "天际蓝", "存储容量": "12GB+512GB"}', 3999.00, 99, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI K90 Max' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'K90MAX-BLUE-12-512');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'K90MAX-SILVER-12-512', '{"颜色": "太空银", "存储容量": "12GB+512GB"}', 3999.00, 99, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI K90 Max' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'K90MAX-SILVER-12-512');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'K90MAX-BLACK-16-256', '{"颜色": "暗影黑", "存储容量": "16GB+256GB"}', 3799.00, 99, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI K90 Max' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'K90MAX-BLACK-16-256');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'K90MAX-BLUE-16-256', '{"颜色": "天际蓝", "存储容量": "16GB+256GB"}', 3799.00, 99, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI K90 Max' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'K90MAX-BLUE-16-256');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'K90MAX-SILVER-16-256', '{"颜色": "太空银", "存储容量": "16GB+256GB"}', 3799.00, 99, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI K90 Max' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'K90MAX-SILVER-16-256');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'K90MAX-BLACK-16-512', '{"颜色": "暗影黑", "存储容量": "16GB+512GB"}', 4299.00, 99, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI K90 Max' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'K90MAX-BLACK-16-512');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'K90MAX-BLUE-16-512', '{"颜色": "天际蓝", "存储容量": "16GB+512GB"}', 4299.00, 99, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI K90 Max' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'K90MAX-BLUE-16-512');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'K90MAX-SILVER-16-512', '{"颜色": "太空银", "存储容量": "16GB+512GB"}', 4299.00, 99, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI K90 Max' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'K90MAX-SILVER-16-512');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'K90MAX-BLACK-16-1TB', '{"颜色": "暗影黑", "存储容量": "16GB+1TB"}', 4999.00, 99, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI K90 Max' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'K90MAX-BLACK-16-1TB');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'K90MAX-BLUE-16-1TB', '{"颜色": "天际蓝", "存储容量": "16GB+1TB"}', 4999.00, 99, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI K90 Max' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'K90MAX-BLUE-16-1TB');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'K90MAX-SILVER-16-1TB', '{"颜色": "太空银", "存储容量": "16GB+1TB"}', 4999.00, 99, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI K90 Max' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'K90MAX-SILVER-16-1TB');

