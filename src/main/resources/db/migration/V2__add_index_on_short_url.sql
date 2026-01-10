-- Create a table to store URLs with their original and shortened versions
CREATE INDEX idx_short_url ON url(short_url);