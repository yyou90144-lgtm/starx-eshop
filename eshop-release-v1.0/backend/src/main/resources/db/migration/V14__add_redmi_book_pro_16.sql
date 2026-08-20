-- Add REDMI Book Pro 16 2026 from mi.com product_id=24037
INSERT INTO product (category_id, name, subtitle, main_image, detail, status)
SELECT 2, 'REDMI Book Pro 16 2026', '第三代英特尔酷睿Ultra处理器 | 至高80W整机性能释放 | 3.2K专业护眼屏',
       '/api/uploads/products/20-bookpro16-main.png', '{"version":1,"gallery":["/api/uploads/products/20-bookpro16-main.png","/api/uploads/products/20-bookpro16-g1.jpg","/api/uploads/products/20-bookpro16-g2.png","/api/uploads/products/20-bookpro16-g3.png","/api/uploads/products/20-bookpro16-g4.png","/api/uploads/products/20-bookpro16-g5.png","/api/uploads/products/20-bookpro16-g6.png","/api/uploads/products/20-bookpro16-g7.png","/api/uploads/products/20-bookpro16-g8.png"],"highlights":["第三代英特尔酷睿 Ultra 处理器","至高 80W 整机性能释放","3.2K 专业护眼屏","小米商城同款规格演示"],"paragraphs":["REDMI Book Pro 16 2026 面向创作与高效办公，搭载第三代英特尔酷睿 Ultra 处理器，整机性能释放更高。","16 英寸 3.2K 专业护眼屏，兼顾清晰显示与长时间用眼舒适。","下方图文详情还原小米商城可下拉浏览的详情长图（参考 product_id=24037）。"],"detailImages":["/api/uploads/products/20-bookpro16-d01.jpg","/api/uploads/products/20-bookpro16-d02.jpg","/api/uploads/products/20-bookpro16-d03.jpg","/api/uploads/products/20-bookpro16-d04.jpg","/api/uploads/products/20-bookpro16-d05.jpg","/api/uploads/products/20-bookpro16-d06.jpg","/api/uploads/products/20-bookpro16-d07.jpg","/api/uploads/products/20-bookpro16-d08.jpg","/api/uploads/products/20-bookpro16-d09.jpg","/api/uploads/products/20-bookpro16-d10.jpg","/api/uploads/products/20-bookpro16-d11.jpg","/api/uploads/products/20-bookpro16-d12.jpg","/api/uploads/products/20-bookpro16-d13.jpg","/api/uploads/products/20-bookpro16-d14.jpg","/api/uploads/products/20-bookpro16-d15.jpg","/api/uploads/products/20-bookpro16-d16.jpg","/api/uploads/products/20-bookpro16-d17.jpg","/api/uploads/products/20-bookpro16-d18.jpg","/api/uploads/products/20-bookpro16-d19.jpg","/api/uploads/products/20-bookpro16-d20.jpg","/api/uploads/products/20-bookpro16-d21.jpg","/api/uploads/products/20-bookpro16-d22.jpg","/api/uploads/products/20-bookpro16-d23.jpg","/api/uploads/products/20-bookpro16-d24.jpg","/api/uploads/products/20-bookpro16-d25.jpg","/api/uploads/products/20-bookpro16-d26.jpg","/api/uploads/products/20-bookpro16-d27.jpg","/api/uploads/products/20-bookpro16-d28.jpg"],"specs":[{"label":"屏幕","value":"16 英寸 3.2K 专业护眼屏"},{"label":"平台","value":"第三代英特尔酷睿 Ultra"},{"label":"颜色","value":"星辰灰"},{"label":"来源","value":"小米商城 REDMI Book Pro 16 2026"}],"colorGalleries":{"星辰灰":["/api/uploads/products/20-bookpro16-main.png","/api/uploads/products/20-bookpro16-g1.jpg","/api/uploads/products/20-bookpro16-g2.png","/api/uploads/products/20-bookpro16-g3.png","/api/uploads/products/20-bookpro16-g4.png"]},"defaultColor":"星辰灰"}', 'ON_SALE'
WHERE NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'REDMI Book Pro 16 2026');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'BOOKPRO16-U5-325-24-1T', '{"颜色": "星辰灰", "处理器": "U5-325 24+1T"}', 7399.00, 50, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI Book Pro 16 2026' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'BOOKPRO16-U5-325-24-1T');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'BOOKPRO16-U5-338H-32-1T', '{"颜色": "星辰灰", "处理器": "U5-338H 32+1T"}', 8499.00, 50, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI Book Pro 16 2026' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'BOOKPRO16-U5-338H-32-1T');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'BOOKPRO16-X7-358H-32-1T', '{"颜色": "星辰灰", "处理器": "X7-358H 32+1T"}', 9699.00, 50, 'ENABLED'
FROM product p
WHERE p.name = 'REDMI Book Pro 16 2026' AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'BOOKPRO16-X7-358H-32-1T');

