CREATE DATABASE IF NOT EXISTS dentistdss CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'dentistdss'@'%' IDENTIFIED BY 'dentistdss';
GRANT ALL PRIVILEGES ON dentistdss.* TO 'dentistdss'@'%';
FLUSH PRIVILEGES;

-- Use the dentistdss database
