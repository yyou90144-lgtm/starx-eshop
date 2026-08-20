-- Restore Max multi-color / multi-storage SKUs (parity with local preview data-seed).
-- Production V3 only seeded one SKU: 森林绿 / 256GB.

UPDATE product_sku
SET
  sku_code = 'PREVIEW-MAX-B-256',
  specs_json = '{"颜色":"暗影黑","存储容量":"12GB+256GB"}',
  price = 2399.00,
  stock = 99,
  status = 'ENABLED'
WHERE product_id = 5
  AND (sku_code = 'DEMO-PHONE-005' OR sku_code = 'PREVIEW-MAX-B-256');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT 5, 'PREVIEW-MAX-B-512', '{"颜色":"暗影黑","存储容量":"12GB+512GB"}', 2699.00, 99, 'ENABLED'
WHERE NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'PREVIEW-MAX-B-512');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT 5, 'PREVIEW-MAX-B-16-256', '{"颜色":"暗影黑","存储容量":"16GB+256GB"}', 2799.00, 99, 'ENABLED'
WHERE NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'PREVIEW-MAX-B-16-256');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT 5, 'PREVIEW-MAX-B-16-512', '{"颜色":"暗影黑","存储容量":"16GB+512GB"}', 3099.00, 99, 'ENABLED'
WHERE NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'PREVIEW-MAX-B-16-512');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT 5, 'PREVIEW-MAX-W-256', '{"颜色":"祥云白","存储容量":"12GB+256GB"}', 2399.00, 99, 'ENABLED'
WHERE NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'PREVIEW-MAX-W-256');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT 5, 'PREVIEW-MAX-W-512', '{"颜色":"祥云白","存储容量":"12GB+512GB"}', 2699.00, 99, 'ENABLED'
WHERE NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'PREVIEW-MAX-W-512');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT 5, 'PREVIEW-MAX-O-256', '{"颜色":"阳光橙","存储容量":"12GB+256GB"}', 2399.00, 99, 'ENABLED'
WHERE NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'PREVIEW-MAX-O-256');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT 5, 'PREVIEW-MAX-O-512', '{"颜色":"阳光橙","存储容量":"12GB+512GB"}', 2699.00, 99, 'ENABLED'
WHERE NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'PREVIEW-MAX-O-512');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT 5, 'PREVIEW-MAX-L-256', '{"颜色":"海风蓝","存储容量":"12GB+256GB"}', 2399.00, 99, 'ENABLED'
WHERE NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'PREVIEW-MAX-L-256');

INSERT INTO product_sku (product_id, sku_code, specs_json, price, stock, status)
SELECT 5, 'PREVIEW-MAX-L-512', '{"颜色":"海风蓝","存储容量":"12GB+512GB"}', 2699.00, 99, 'ENABLED'
WHERE NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = 'PREVIEW-MAX-L-512');
