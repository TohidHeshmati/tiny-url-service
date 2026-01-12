-- Create a table to store URLs with their original and shortened versions
ALTER TABLE url ADD INDEX idx_short_url (short_url);