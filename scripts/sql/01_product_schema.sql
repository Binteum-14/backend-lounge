-- Product 스키마 보완 + product_variant 신규
-- 실행 순서: 01 -> 02 -> 03
-- product TRUNCATE/재생성 금지 (recommendation_product, visit_pass_product FK)

BEGIN;

-- sku는 기존 row가 있으므로 먼저 nullable로 추가. NOT NULL/UNIQUE는 02 데이터 반영 후 적용.
ALTER TABLE product ADD COLUMN IF NOT EXISTS sku VARCHAR(255);
ALTER TABLE product ADD COLUMN IF NOT EXISTS product_feature TEXT;
ALTER TABLE product ADD COLUMN IF NOT EXISTS care_guide TEXT;
ALTER TABLE product ADD COLUMN IF NOT EXISTS laptop_storage_available BOOLEAN;
ALTER TABLE product ADD COLUMN IF NOT EXISTS laptop_storage_score INTEGER;
ALTER TABLE product ADD COLUMN IF NOT EXISTS cabin_suitability_score INTEGER;
ALTER TABLE product ADD COLUMN IF NOT EXISTS source_collected_at DATE;

ALTER TABLE product DROP COLUMN IF EXISTS laptop_storage_grade;
ALTER TABLE product DROP COLUMN IF EXISTS cabin_suitability_grade;
ALTER TABLE product DROP COLUMN IF EXISTS waterproof_grade;

CREATE TABLE IF NOT EXISTS product_variant (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    sku VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    color VARCHAR(255),
    price BIGINT,
    image_url VARCHAR(255),
    detail_url TEXT,
    representative BOOLEAN,
    active BOOLEAN,
    source_collected_at DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_product_variant_product
        FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT uk_product_variant_sku UNIQUE (sku)
);

CREATE INDEX IF NOT EXISTS idx_product_variant_product_id ON product_variant (product_id);

COMMIT;
