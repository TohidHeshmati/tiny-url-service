CREATE DATABASE IF NOT EXISTS tiny_url_service_local;
CREATE DATABASE IF NOT EXISTS tiny_url_service_test;
GRANT ALL PRIVILEGES ON tiny_url_service_local.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON tiny_url_service_test.* TO 'root'@'%';
FLUSH PRIVILEGES;
