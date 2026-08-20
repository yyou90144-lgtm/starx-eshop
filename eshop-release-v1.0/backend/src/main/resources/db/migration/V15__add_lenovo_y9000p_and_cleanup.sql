-- Add Lenovo Legion Y9000P 2026 + retire casual demo products
INSERT INTO product (category_id, name, subtitle, main_image, detail, status)
SELECT 2, '联想拯救者 Y9000P 2026 至尊版', '英特尔酷睿 Ultra 9 290HX Plus｜64GB｜2TB SSD｜RTX 5090 24G｜碳晶黑', '/api/uploads/products/21-y9000p-g1.jpg', '{"version":1,"gallery":["/api/uploads/products/21-y9000p-g1.jpg","/api/uploads/products/21-y9000p-g2.jpg","/api/uploads/products/21-y9000p-g3.jpg","/api/uploads/products/21-y9000p-g4.jpg","/api/uploads/products/21-y9000p-g5.jpg","/api/uploads/products/21-y9000p-g6.jpg","/api/uploads/products/21-y9000p-g7.jpg","/api/uploads/products/21-y9000p-g8.jpg"],"highlights":["酷睿 Ultra 9 290HX Plus","RTX 5090 24G 电竞显卡","64GB + 2TB 旗舰存储","16 英寸拯救者电竞本"],"paragraphs":["联想拯救者 Y9000P 2026 至尊版面向顶级游戏与创作场景，搭载英特尔酷睿 Ultra 9 与 RTX 5090 显卡。","标配 64GB 内存与 2TB SSD，碳晶黑配色。本页信息参考联想商城商品 1054240，用于星选商城演示。","下方图文详情按官网详情页顺序合成连续长图。"],"detailImages":["/api/uploads/products/21-y9000p-detail-full.jpg"],"specs":[{"label":"处理器","value":"英特尔 酷睿 Ultra 9 290HX Plus"},{"label":"显卡","value":"RTX 5090 24G"},{"label":"内存/硬盘","value":"64GB / 2TB SSD"},{"label":"颜色","value":"碳晶黑"},{"label":"来源","value":"联想商城 1054240"}],"colorGalleries":{"碳晶黑":["/api/uploads/products/21-y9000p-g1.jpg","/api/uploads/products/21-y9000p-g2.jpg","/api/uploads/products/21-y9000p-g3.jpg","/api/uploads/products/21-y9000p-g4.jpg"]},"defaultColor":"碳晶黑"}', 'ON_SALE'
WHERE NOT EXISTS (SELECT 1 FROM product p WHERE p.name = '联想拯救者 Y9000P 2026 至尊版');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'Y9000P-2026-U9-64-2T', '{"颜色":"碳晶黑","配置":"Ultra9/64GB/2TB/RTX5090"}', 44999.00, 20, 'ENABLED'
FROM product p WHERE p.name = '联想拯救者 Y9000P 2026 至尊版'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'Y9000P-2026-U9-64-2T');

-- Soft-remove casual demo products (keep specially crafted ones)
UPDATE product SET status = 'OFF_SALE'
WHERE name NOT IN ('长续航手机 Max', 'REDMI K90 Max', 'REDMI Book Pro 16 2026', '联想拯救者 Y9000P 2026 至尊版');

