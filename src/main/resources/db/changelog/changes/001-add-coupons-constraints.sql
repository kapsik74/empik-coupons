ALTER TABLE coupons
    ADD CONSTRAINT chk_max_uses_positive CHECK (max_uses > 0);

ALTER TABLE coupons
    ADD CONSTRAINT chk_current_uses_non_negative CHECK (current_uses >= 0);

ALTER TABLE coupons
    ADD CONSTRAINT chk_current_uses_not_exceed_max CHECK (current_uses <= max_uses);