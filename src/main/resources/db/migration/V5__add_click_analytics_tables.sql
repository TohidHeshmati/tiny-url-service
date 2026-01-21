-- Add column to existing table
ALTER TABLE url
    ADD COLUMN total_click_count BIGINT NOT NULL DEFAULT 0;

-- Create daily clicks table
CREATE TABLE url_daily_clicks
(
    id         BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
    url_id     BIGINT   NOT NULL,
    click_date DATE     NOT NULL,
    count      BIGINT   NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    device_type VARCHAR(20) NOT NULL DEFAULT 'OTHER',
    CONSTRAINT fk_url_daily FOREIGN KEY (url_id) REFERENCES url (id) ON DELETE CASCADE,
    UNIQUE KEY (url_id, click_date)
);

-- Create hourly clicks table
CREATE TABLE url_hourly_clicks
(
    id          BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    url_id      BIGINT      NOT NULL,
    click_hour  TIMESTAMP   NOT NULL,
    count       BIGINT      NOT NULL DEFAULT 0,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    device_type VARCHAR(20) NOT NULL DEFAULT 'OTHER',
        CONSTRAINT fk_url_hourly FOREIGN KEY (url_id) REFERENCES url (id) ON DELETE CASCADE,
    UNIQUE KEY (url_id, click_hour)
);

ALTER TABLE url_daily_clicks ADD CONSTRAINT uq_url_daily_device UNIQUE (url_id, click_date, device_type);
ALTER TABLE url_hourly_clicks ADD CONSTRAINT uq_url_hourly_device UNIQUE (url_id, click_hour, device_type);
ALTER TABLE url_daily_clicks DROP INDEX url_id;
ALTER TABLE url_hourly_clicks DROP INDEX url_id;