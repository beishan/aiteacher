-- 课时费阶梯价格：按时长定价，如 1小时200元、2小时380元
ALTER TABLE student_fees ADD COLUMN price_tiers VARCHAR(1000);
COMMENT ON COLUMN student_fees.price_tiers IS '阶梯价格JSON数组，格式：[{"hours":1,"price":200},{"hours":2,"price":380}]';
