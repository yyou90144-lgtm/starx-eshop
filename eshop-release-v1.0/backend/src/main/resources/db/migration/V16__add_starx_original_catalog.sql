-- STARX original demo products (generated imagery, learning-only)
SET NAMES utf8mb4;

INSERT INTO product (category_id, name, subtitle, main_image, detail, status)
SELECT c.id, 'STARX Pulse 开放式耳机', '不入耳开放聆听｜蓝牙 5.4｜36 小时续航', '/api/uploads/products/30-pulse-g1.jpg', '{"version":1,"gallery":["/api/uploads/products/30-pulse-g1.jpg","/api/uploads/products/30-pulse-g2.jpg"],"highlights":["开放式耳挂，通勤听环境音","蓝牙 5.4 低延迟","单耳 8 小时，充电盒合计 36 小时","IPX4 防溅"],"paragraphs":["STARX Pulse 面向通勤和轻度运动：耳挂贴合耳廓，不堵住耳道，路过提醒和车流声仍听得见。","驱动单元做了轻量化，长时间佩戴更不容易压耳。充电盒小到可以和钥匙一起放进口袋。","本页图文为星选商城原创演示，不是第三方品牌官方商品。"],"detailImages":["/api/uploads/products/30-pulse-d1.jpg"],"specs":[{"label":"形态","value":"开放式耳挂"},{"label":"连接","value":"蓝牙 5.4"},{"label":"续航","value":"单耳 8h / 合计 36h"},{"label":"防护","value":"IPX4"}],"colorGalleries":{"石墨灰":["/api/uploads/products/30-pulse-g1.jpg","/api/uploads/products/30-pulse-g2.jpg"]},"defaultColor":"石墨灰","learningNote":"本商品为星选 STARX 自有演示款，图文为原创生成，仅供学习演示，不代表任何第三方品牌。","sourceNote":"STARX 原创演示"}', 'ON_SALE'
FROM category c
WHERE c.name = '手机数码' AND c.parent_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'STARX Pulse 开放式耳机')
LIMIT 1;

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'STARX-PULSE-GRAPHITE', '{"颜色":"石墨灰"}', 399.00, 80, 'ENABLED'
FROM product p WHERE p.name = 'STARX Pulse 开放式耳机'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'STARX-PULSE-GRAPHITE');

INSERT INTO product (category_id, name, subtitle, main_image, detail, status)
SELECT c.id, 'STARX Arc S 智能手表', '1.43 英寸圆形屏｜5ATM｜双频 GPS｜10 天续航', '/api/uploads/products/31-arc-g1.jpg', '{"version":1,"gallery":["/api/uploads/products/31-arc-g1.jpg","/api/uploads/products/31-arc-g2.jpg"],"highlights":["1.43 英寸圆形 AMOLED","5ATM 防水","双频 GPS 轨迹更稳","典型使用约 10 天"],"paragraphs":["STARX Arc S 把日常健康和轻运动收进一只手表：心率、睡眠、血氧和运动模式都在手腕上完成。","表体用喷砂铝，表带是亲肤氟橡胶，洗手、小雨和泳池浅水都可以戴着。","本页图文为星选商城原创演示，不是第三方品牌官方商品。"],"detailImages":["/api/uploads/products/31-arc-d1.jpg"],"specs":[{"label":"屏幕","value":"1.43 英寸圆形 AMOLED"},{"label":"防水","value":"5ATM"},{"label":"定位","value":"双频 GPS"},{"label":"续航","value":"典型使用约 10 天"}],"colorGalleries":{"石墨灰":["/api/uploads/products/31-arc-g1.jpg","/api/uploads/products/31-arc-g2.jpg"]},"defaultColor":"石墨灰","learningNote":"本商品为星选 STARX 自有演示款，图文为原创生成，仅供学习演示，不代表任何第三方品牌。","sourceNote":"STARX 原创演示"}', 'ON_SALE'
FROM category c
WHERE c.name = '手机数码' AND c.parent_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'STARX Arc S 智能手表')
LIMIT 1;

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'STARX-ARC-S-GRAPHITE', '{"颜色":"石墨灰","表带":"氟橡胶"}', 899.00, 60, 'ENABLED'
FROM product p WHERE p.name = 'STARX Arc S 智能手表'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'STARX-ARC-S-GRAPHITE');

INSERT INTO product (category_id, name, subtitle, main_image, detail, status)
SELECT c.id, 'STARX Volt 65 氮化镓充电头', '65W 双 C 口｜折叠插脚｜笔记本也能充', '/api/uploads/products/32-volt-g1.jpg', '{"version":1,"gallery":["/api/uploads/products/32-volt-g1.jpg","/api/uploads/products/32-volt-g2.jpg"],"highlights":["65W 氮化镓，体积更小","双 USB-C，可一充两台","折叠插脚方便收纳","支持常见快充协议"],"paragraphs":["STARX Volt 65 把出差充电头收成一块积木：双 USB-C，一台笔电加一部手机可以同时走。","插脚可折叠，扔进行李袋不刮别的东西。外壳是暖白色，和星选店铺的暖白底更搭。","本页图文为星选商城原创演示，不是第三方品牌官方商品。"],"detailImages":["/api/uploads/products/32-volt-d1.jpg"],"specs":[{"label":"功率","value":"65W"},{"label":"接口","value":"USB-C × 2"},{"label":"材质","value":"氮化镓"},{"label":"插脚","value":"可折叠"}],"defaultColor":"暖沙白","colorGalleries":{"暖沙白":["/api/uploads/products/32-volt-g1.jpg","/api/uploads/products/32-volt-g2.jpg"]},"learningNote":"本商品为星选 STARX 自有演示款，图文为原创生成，仅供学习演示，不代表任何第三方品牌。","sourceNote":"STARX 原创演示"}', 'ON_SALE'
FROM category c
WHERE c.name = '手机数码' AND c.parent_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'STARX Volt 65 氮化镓充电头')
LIMIT 1;

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'STARX-VOLT-65-SAND', '{"颜色":"暖沙白"}', 149.00, 120, 'ENABLED'
FROM product p WHERE p.name = 'STARX Volt 65 氮化镓充电头'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'STARX-VOLT-65-SAND');

INSERT INTO product (category_id, name, subtitle, main_image, detail, status)
SELECT c.id, 'STARX Keyline 75 机械键盘', '75% 配列｜热插拔｜铝壳灯带｜双模连接', '/api/uploads/products/33-keyline-g1.jpg', '{"version":1,"gallery":["/api/uploads/products/33-keyline-g1.jpg","/api/uploads/products/33-keyline-g2.jpg"],"highlights":["75% 配列，方向键都在","热插拔轴座，可换轴","喷砂铝壳 + 底部暖光灯带","2.4G / 蓝牙 / 有线三模"],"paragraphs":["STARX Keyline 75 给桌面留出鼠标空间，又不丢掉方向键和 F 区常用键。","PBT 键帽是暖米色拼色，灯带只在底板走一圈暖光，晚上不刺眼。轴体出厂可选线性红轴或段落茶轴。","本页图文为星选商城原创演示，不是第三方品牌官方商品。"],"detailImages":["/api/uploads/products/33-keyline-d1.jpg"],"specs":[{"label":"配列","value":"75%"},{"label":"连接","value":"三模"},{"label":"轴座","value":"热插拔"},{"label":"壳体","value":"喷砂铝"}],"defaultColor":"暖沙拼色","colorGalleries":{"暖沙拼色":["/api/uploads/products/33-keyline-g1.jpg","/api/uploads/products/33-keyline-g2.jpg"]},"learningNote":"本商品为星选 STARX 自有演示款，图文为原创生成，仅供学习演示，不代表任何第三方品牌。","sourceNote":"STARX 原创演示"}', 'ON_SALE'
FROM category c
WHERE c.name = '电脑办公' AND c.parent_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'STARX Keyline 75 机械键盘')
LIMIT 1;

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'STARX-KEYLINE-75-RED', '{"颜色":"暖沙拼色","轴体":"线性红轴"}', 499.00, 45, 'ENABLED'
FROM product p WHERE p.name = 'STARX Keyline 75 机械键盘'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'STARX-KEYLINE-75-RED');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'STARX-KEYLINE-75-BROWN', '{"颜色":"暖沙拼色","轴体":"段落茶轴"}', 499.00, 40, 'ENABLED'
FROM product p WHERE p.name = 'STARX Keyline 75 机械键盘'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'STARX-KEYLINE-75-BROWN');

INSERT INTO product (category_id, name, subtitle, main_image, detail, status)
SELECT c.id, 'STARX Trace 静音鼠标', '静音微动｜双模｜8000 DPI｜全天续航', '/api/uploads/products/34-trace-g1.jpg', '{"version":1,"gallery":["/api/uploads/products/34-trace-g1.jpg","/api/uploads/products/34-trace-g2.jpg"],"highlights":["办公室友好的静音微动","2.4G + 蓝牙双模","DPI 可调至 8000","单次充电可用约 40 天"],"paragraphs":["STARX Trace 给共享工位和夜里加班用：按键是静音微动，旁边同事不容易被点按声打断。","握持偏中小手，侧裙是暖沙色防滑，传感器在布垫和木桌上都比较稳。","本页图文为星选商城原创演示，不是第三方品牌官方商品。"],"detailImages":["/api/uploads/products/34-trace-d1.jpg"],"specs":[{"label":"微动","value":"静音"},{"label":"连接","value":"2.4G / 蓝牙"},{"label":"DPI","value":"最高 8000"},{"label":"续航","value":"约 40 天"}],"defaultColor":"石墨灰","colorGalleries":{"石墨灰":["/api/uploads/products/34-trace-g1.jpg","/api/uploads/products/34-trace-g2.jpg"]},"learningNote":"本商品为星选 STARX 自有演示款，图文为原创生成，仅供学习演示，不代表任何第三方品牌。","sourceNote":"STARX 原创演示"}', 'ON_SALE'
FROM category c
WHERE c.name = '电脑办公' AND c.parent_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'STARX Trace 静音鼠标')
LIMIT 1;

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'STARX-TRACE-GRAPHITE', '{"颜色":"石墨灰"}', 199.00, 90, 'ENABLED'
FROM product p WHERE p.name = 'STARX Trace 静音鼠标'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'STARX-TRACE-GRAPHITE');

INSERT INTO product (category_id, name, subtitle, main_image, detail, status)
SELECT c.id, 'STARX Halo 护眼台灯', '无频闪｜四档色温｜触摸调光｜圆形灯头', '/api/uploads/products/35-halo-g1.jpg', '{"version":1,"gallery":["/api/uploads/products/35-halo-g1.jpg","/api/uploads/products/35-halo-g2.jpg"],"highlights":["无频闪读写灯光","2700K–5000K 四档色温","触摸调光，记忆上次亮度","配重底座，单手转灯头不倒"],"paragraphs":["STARX Halo 给阅读和桌面工作补一层均匀的暖光。圆形灯头把光线铺开，桌面阴影更少。","铝臂可以单手拉开，底座有配重。色温从夜读暖光到白天读写白光都能切。","本页图文为星选商城原创演示，不是第三方品牌官方商品。"],"detailImages":["/api/uploads/products/35-halo-d1.jpg"],"specs":[{"label":"光源","value":"无频闪 LED"},{"label":"色温","value":"2700K–5000K"},{"label":"调光","value":"触摸无级"},{"label":"功率","value":"12W"}],"defaultColor":"石墨灰","colorGalleries":{"石墨灰":["/api/uploads/products/35-halo-g1.jpg","/api/uploads/products/35-halo-g2.jpg"]},"learningNote":"本商品为星选 STARX 自有演示款，图文为原创生成，仅供学习演示，不代表任何第三方品牌。","sourceNote":"STARX 原创演示"}', 'ON_SALE'
FROM category c
WHERE c.name = '家居生活' AND c.parent_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'STARX Halo 护眼台灯')
LIMIT 1;

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'STARX-HALO-GRAPHITE', '{"颜色":"石墨灰"}', 259.00, 70, 'ENABLED'
FROM product p WHERE p.name = 'STARX Halo 护眼台灯'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'STARX-HALO-GRAPHITE');

INSERT INTO product (category_id, name, subtitle, main_image, detail, status)
SELECT c.id, 'STARX Drift Mini 便携音箱', '360° 织物声场｜IPX5｜12 小时续航', '/api/uploads/products/36-drift-g1.jpg', '{"version":1,"gallery":["/api/uploads/products/36-drift-g1.jpg","/api/uploads/products/36-drift-g2.jpg"],"highlights":["掌心尺寸，360° 织物出声","IPX5 防溅，阳台也能放","续航约 12 小时","蓝牙 5.3，双台串联"],"paragraphs":["STARX Drift Mini 适合厨房台面、窗台和短途出门。织物网罩是暖沙色，声音往四周铺，不是怼着一张脸喊。","机身轻，单手能握住。轻度水溅和湿手操作都还可以接受。","本页图文为星选商城原创演示，不是第三方品牌官方商品。"],"detailImages":["/api/uploads/products/36-drift-d1.jpg"],"specs":[{"label":"形态","value":"便携织物音箱"},{"label":"防护","value":"IPX5"},{"label":"续航","value":"约 12 小时"},{"label":"连接","value":"蓝牙 5.3"}],"defaultColor":"石墨灰","colorGalleries":{"石墨灰":["/api/uploads/products/36-drift-g1.jpg","/api/uploads/products/36-drift-g2.jpg"]},"learningNote":"本商品为星选 STARX 自有演示款，图文为原创生成，仅供学习演示，不代表任何第三方品牌。","sourceNote":"STARX 原创演示"}', 'ON_SALE'
FROM category c
WHERE c.name = '家居生活' AND c.parent_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'STARX Drift Mini 便携音箱')
LIMIT 1;

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'STARX-DRIFT-GRAPHITE', '{"颜色":"石墨灰"}', 329.00, 75, 'ENABLED'
FROM product p WHERE p.name = 'STARX Drift Mini 便携音箱'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'STARX-DRIFT-GRAPHITE');

INSERT INTO product (category_id, name, subtitle, main_image, detail, status)
SELECT c.id, 'STARX Airy 无线吸尘器', '轻量杆身｜150AW｜60 分钟｜绿光显尘', '/api/uploads/products/37-airy-g1.jpg', '{"version":1,"gallery":["/api/uploads/products/37-airy-g1.jpg","/api/uploads/products/37-airy-g2.jpg"],"highlights":["整机更轻，单手抬杆不费劲","150AW 吸力，地毯和地板都能扫","最长约 60 分钟","绿光显尘，浅色地板更明显"],"paragraphs":["STARX Airy 给小户型日常地面：杆身细，尘杯半透明，清倒时能看见积了多少。","地刷带绿光，浅色木地板上的碎屑会亮出来。电池可拆，继续吸的时候能换备用电池。","本页图文为星选商城原创演示，不是第三方品牌官方商品。"],"detailImages":["/api/uploads/products/37-airy-d1.jpg"],"specs":[{"label":"吸力","value":"150AW"},{"label":"续航","value":"最长约 60 分钟"},{"label":"尘杯","value":"0.5L 可水洗"},{"label":"地刷","value":"绿光显尘"}],"defaultColor":"石墨灰","colorGalleries":{"石墨灰":["/api/uploads/products/37-airy-g1.jpg","/api/uploads/products/37-airy-g2.jpg"]},"learningNote":"本商品为星选 STARX 自有演示款，图文为原创生成，仅供学习演示，不代表任何第三方品牌。","sourceNote":"STARX 原创演示"}', 'ON_SALE'
FROM category c
WHERE c.name = '家居生活' AND c.parent_id IS NULL
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.name = 'STARX Airy 无线吸尘器')
LIMIT 1;

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT p.id, 'STARX-AIRY-STD', '{"颜色":"石墨灰","套装":"标准地刷"}', 1299.00, 35, 'ENABLED'
FROM product p WHERE p.name = 'STARX Airy 无线吸尘器'
  AND NOT EXISTS (SELECT 1 FROM product_sku s WHERE s.sku_code = 'STARX-AIRY-STD');
