-- 去掉「演示」字样，让前台展示更接近商业商城
UPDATE category
SET name = REPLACE(name, '演示分类-', '')
WHERE name LIKE '演示分类-%';

UPDATE product
SET name = TRIM(REPLACE(name, '演示', ''))
WHERE name LIKE '演示%';
