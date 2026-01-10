-- Create a table to store URLs with their original and shortened versions
CREATE TABLE url
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_url VARCHAR(512) NOT NULL UNIQUE,
    short_url    VARCHAR(20)  NOT NULL UNIQUE,
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiry_date  DATETIME   NULL
);