CREATE TABLE client
(
    client_id             INT AUTO_INCREMENT PRIMARY KEY,
    client_name           VARCHAR(255) NOT NULL UNIQUE
);


CREATE TABLE client_url
(
    client_url_id         INT AUTO_INCREMENT PRIMARY KEY,
    client_url            VARCHAR(500) NULL UNIQUE,
    client_name           VARCHAR(255) NOT NULL,
    registered            CHAR(1) NULL DEFAULT 0
);

CREATE TABLE category
(
    category_id           INT AUTO_INCREMENT PRIMARY KEY,
    category_name         VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE domain
(
    domain_id        INT AUTO_INCREMENT PRIMARY KEY,
    category_name    VARCHAR(255) NULL,
    domain           VARCHAR(500) NOT NULL,
    uploaded_at      TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY (category_name, domain)
);


CREATE TABLE page_url
(
    page_url_id        INT AUTO_INCREMENT PRIMARY KEY,
    client_url         VARCHAR(500) NULL,
    domain_id          INT NULL,
    page_url           VARCHAR(500) NULL UNIQUE ,
    category_name      VARCHAR(255) NULL,
    domain             VARCHAR(500) NOT NULL,
    published_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
