CREATE TABLE bank_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    card_number VARCHAR(19) NOT NULL,
    card_holder_name VARCHAR(100) NOT NULL,
    expiry_date VARCHAR(5) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_card_number CHECK (card_number REGEXP '^[0-9]{16,19}$'),
    CONSTRAINT chk_expiry_date CHECK (expiry_date REGEXP '^(0[1-9]|1[0-2])/[0-9]{2}$')
);

-- 添加索引
CREATE INDEX idx_bank_cards_user_id ON bank_cards(user_id);
CREATE INDEX idx_bank_cards_is_default ON bank_cards(is_default); 