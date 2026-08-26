-- class_members inherits BaseEntity fields in the application model.
-- Add the audit columns omitted by the original V7 table definition.
ALTER TABLE class_members
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE class_members
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

COMMENT ON COLUMN class_members.created_at IS '创建时间';
COMMENT ON COLUMN class_members.updated_at IS '更新时间';
