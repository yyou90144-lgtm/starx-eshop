-- Restore real catalog images (replace V3 SVG placeholders).
-- Match by id (stable V3 insert order) to avoid charset/name mismatches.

UPDATE product SET main_image = '/api/demo/products/01-phone-a1.jpg' WHERE id = 1;
UPDATE product SET main_image = '/api/demo/products/02-phone-pro.webp' WHERE id = 2;
UPDATE product SET main_image = '/api/demo/products/03-phone-mini.jpg' WHERE id = 3;
UPDATE product SET main_image = '/api/demo/products/04-phone-ultra.png' WHERE id = 4;
UPDATE product SET main_image = '/api/demo/products/05-max-angle-1-full.jpg' WHERE id = 5;
UPDATE product SET main_image = '/api/demo/products/06-phone-5g.jpg' WHERE id = 6;
UPDATE product SET main_image = '/api/demo/products/04-phone-ultra.png' WHERE id = 7;
UPDATE product SET main_image = '/api/demo/products/02-phone-pro.webp' WHERE id = 8;
UPDATE product SET main_image = '/api/demo/products/01-phone-a1.jpg' WHERE id = 9;
UPDATE product SET main_image = '/api/demo/products/03-phone-mini.jpg' WHERE id = 10;
UPDATE product SET main_image = '/api/demo/products/07-laptop-air.jpg' WHERE id = 11;
UPDATE product SET main_image = '/api/demo/products/08-laptop-gaming.jpg' WHERE id = 12;
UPDATE product SET main_image = '/api/demo/products/09-tablet-pad.jpg' WHERE id = 13;
UPDATE product SET main_image = '/api/demo/products/10-monitor-27.jpg' WHERE id = 14;
UPDATE product SET main_image = '/api/demo/products/11-keyboard-k87.jpg' WHERE id = 15;
UPDATE product SET main_image = '/api/demo/products/12-mouse-m2.jpg' WHERE id = 16;
UPDATE product SET main_image = '/api/demo/products/09-tablet-pad.jpg' WHERE id = 17;
UPDATE product SET main_image = '/api/demo/products/10-monitor-27.jpg' WHERE id = 18;
UPDATE product SET main_image = '/api/demo/products/11-keyboard-k87.jpg' WHERE id = 19;
UPDATE product SET main_image = '/api/demo/products/12-mouse-m2.jpg' WHERE id = 20;
UPDATE product SET main_image = '/api/demo/products/13-lamp.webp' WHERE id = 21;
UPDATE product SET main_image = '/api/demo/products/14-air-purifier.jpg' WHERE id = 22;
UPDATE product SET main_image = '/api/demo/products/15-vacuum.jpg' WHERE id = 23;
UPDATE product SET main_image = '/api/demo/products/16-rice-cooker.jpg' WHERE id = 24;
UPDATE product SET main_image = '/api/demo/products/17-kettle.png' WHERE id = 25;
UPDATE product SET main_image = '/api/demo/products/18-speaker.jpg' WHERE id = 26;
UPDATE product SET main_image = '/api/demo/products/14-air-purifier.jpg' WHERE id = 27;
UPDATE product SET main_image = '/api/demo/products/18-speaker.jpg' WHERE id = 28;
UPDATE product SET main_image = '/api/demo/products/15-vacuum.jpg' WHERE id = 29;
UPDATE product SET main_image = '/api/demo/products/17-kettle.png' WHERE id = 30;

-- Max: multi-angle gallery + 4-color galleries (same as local preview seed)
UPDATE product
SET
  subtitle = '9000mAh 大电池 · 天玑 9500s · 1.5K 阳光屏',
  main_image = '/api/demo/products/05-max-angle-1-full.jpg',
  detail = '{"version":1,"gallery":["/api/demo/products/05-max-angle-1-full.jpg","/api/demo/products/05-max-angle-2-back.jpg","/api/demo/products/05-max-angle-3-front.jpg","/api/demo/products/05-max-angle-4-side.jpg"],"highlights":["9000mAh 级长续航体验","高性能芯 · 流畅日常","1.5K 高亮阳光屏","差旅与重度使用更友好"],"paragraphs":["长续航手机 Max 面向重度使用与差旅场景，强调充一次用更久。","处理器采用高性能移动平台，兼顾流畅与能效；1.5K 级高亮显示。","机身工艺与配色针对长时间握持做了圆润处理。","本页展示图用于商城演示。"],"detailImages":["/api/demo/products/05-max-angle-1-full.jpg","/api/demo/products/05-max-angle-2-back.jpg","/api/demo/products/05-max-angle-3-front.jpg","/api/demo/products/05-max-angle-4-side.jpg"],"specs":[{"label":"电池","value":"大容量长续航方案（参考 9000mAh 级）"},{"label":"屏幕","value":"1.5K 级高亮显示"},{"label":"平台","value":"高性能移动处理器"}],"colorGalleries":{"暗影黑":["/api/demo/products/05-max-angle-1-full.jpg","/api/demo/products/05-max-angle-2-back.jpg","/api/demo/products/05-max-angle-3-front.jpg","/api/demo/products/05-max-angle-4-side.jpg"],"祥云白":["/api/demo/products/05-max-white-full.jpg","/api/demo/products/05-max-white-back.jpg","/api/demo/products/05-max-white-front.jpg","/api/demo/products/05-max-white-side.jpg"],"阳光橙":["/api/demo/products/05-max-orange-full.jpg","/api/demo/products/05-max-orange-back.jpg","/api/demo/products/05-max-orange-front.jpg","/api/demo/products/05-max-orange-side.jpg"],"海风蓝":["/api/demo/products/05-max-blue-full.jpg","/api/demo/products/05-max-blue-back.jpg","/api/demo/products/05-max-blue-front.jpg","/api/demo/products/05-max-blue-side.jpg"]},"defaultColor":"暗影黑"}'
WHERE id = 5;
