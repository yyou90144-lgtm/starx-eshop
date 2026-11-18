-- Public catalog keeps only the four crafted products.
UPDATE product
SET status = 'OFF_SALE'
WHERE name NOT IN (
    '长续航手机 Max',
    'REDMI K90 Max',
    'REDMI Book Pro 16 2026',
    '联想拯救者 Y9000P 2026 至尊版'
);

-- Drop the STARX original demo SKUs/products added in V16.
DELETE FROM product_sku
WHERE product_id IN (
    SELECT id FROM product WHERE name LIKE 'STARX %'
);

DELETE FROM product
WHERE name LIKE 'STARX %';
