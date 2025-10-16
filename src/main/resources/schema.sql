DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
    id                  BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username            VARCHAR(255) NOT NULL,
    password            VARCHAR(255) NOT NULL,
    role                VARCHAR(255) NOT NULL
);