-- Migração manual para PostgreSQL. Aplicar antes de iniciar esta versão em ambiente com ddl-auto=validate.
-- A prévia local usa Hibernate update. Não há execução automática deste arquivo.
BEGIN;
ALTER TABLE professional ADD COLUMN IF NOT EXISTS description varchar(2000);
ALTER TABLE professional ADD COLUMN IF NOT EXISTS starting_price numeric(12,2);
ALTER TABLE professional ADD COLUMN IF NOT EXISTS accepting_requests boolean NOT NULL DEFAULT true;
CREATE TABLE IF NOT EXISTS service_request (
 id bigserial PRIMARY KEY, customer_id bigint NOT NULL REFERENCES tb_users(id),
 professional_id bigint NOT NULL REFERENCES professional(id), description varchar(2000) NOT NULL,
 preferred_date date, status varchar(30) NOT NULL,
 created_at timestamptz NOT NULL, updated_at timestamptz NOT NULL, request_key varchar(36) NOT NULL,
 UNIQUE(customer_id,request_key),
 CHECK(status IN ('REQUESTED','ACCEPTED','DECLINED','AWAITING_CONFIRMATION','COMPLETED','CANCELLED'))
);
CREATE TABLE IF NOT EXISTS service_review (
 id bigserial PRIMARY KEY, request_id bigint NOT NULL UNIQUE REFERENCES service_request(id),
 rating integer NOT NULL CHECK(rating BETWEEN 1 AND 5), comment varchar(1000) NOT NULL,
 created_at timestamptz NOT NULL, visible boolean NOT NULL DEFAULT true
);
CREATE TABLE IF NOT EXISTS service_favorite (
 id bigserial PRIMARY KEY, customer_id bigint NOT NULL REFERENCES tb_users(id),
 professional_id bigint NOT NULL REFERENCES professional(id), UNIQUE(customer_id,professional_id)
);
CREATE INDEX IF NOT EXISTS idx_service_request_customer ON service_request(customer_id,created_at DESC);
CREATE INDEX IF NOT EXISTS idx_service_request_professional ON service_request(professional_id,created_at DESC);
COMMIT;
