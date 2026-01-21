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
    CONSTRAINT fk_url_daily FOREIGN KEY (url_id) REFERENCES url (id) ON DELETE CASCADE,
    UNIQUE KEY (url_id, click_date)
);

-- Create hourly clicks table
CREATE TABLE url_hourly_clicks
(
    id         BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
    url_id     BIGINT    NOT NULL,
    click_hour TIMESTAMP NOT NULL,
    count      BIGINT    NOT NULL DEFAULT 0,
    created_at DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_url_hourly FOREIGN KEY (url_id) REFERENCES url (id) ON DELETE CASCADE,
    UNIQUE KEY (url_id, click_hour)
);

-- but explicit naming is good practice)
CREATE INDEX idx_url_daily_clicks_url_id_click_date ON url_daily_clicks (url_id, click_date);
CREATE INDEX idx_url_hourly_clicks_url_id_click_hour ON url_hourly_clicks (url_id, click_hour);