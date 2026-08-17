-- snack_set.product_id -> product_variant_id
-- 기존 row는 해당 product의 representative variant(없으면 id 가장 작은 variant)로 매핑

BEGIN;

ALTER TABLE snack_set ADD COLUMN IF NOT EXISTS product_variant_id BIGINT;

UPDATE snack_set ss
SET product_variant_id = mapped.variant_id
FROM (
    SELECT DISTINCT ON (pv.product_id)
        pv.product_id,
        pv.id AS variant_id
    FROM product_variant pv
    ORDER BY pv.product_id, pv.representative DESC NULLS LAST, pv.id ASC
) mapped
WHERE ss.product_id = mapped.product_id
  AND ss.product_variant_id IS NULL;

ALTER TABLE snack_set
    ALTER COLUMN product_variant_id SET NOT NULL;

ALTER TABLE snack_set
    ADD CONSTRAINT fk_snack_set_product_variant
        FOREIGN KEY (product_variant_id) REFERENCES product_variant (id);

CREATE INDEX IF NOT EXISTS idx_snack_set_product_variant_id ON snack_set (product_variant_id);

ALTER TABLE snack_set DROP COLUMN IF EXISTS product_id;

COMMIT;
