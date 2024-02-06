CREATE TABLE client
(
    client_id             INT AUTO_INCREMENT PRIMARY KEY,
    client_name           VARCHAR(255) NOT NULL UNIQUE
);


CREATE TABLE client_url
(
    client_url_id         INT AUTO_INCREMENT PRIMARY KEY,
    client_url            VARCHAR(1000) NULL UNIQUE,
    client_name           VARCHAR(255) NOT NULL,
    registered            CHAR(1) NULL
);

CREATE TABLE category
(
    category_id           INT AUTO_INCREMENT PRIMARY KEY,
    category_name         VARCHAR(255) NOT NULL UNIQUE,
    domain                VARCHAR(1000) NULL
);

CREATE TABLE domain
(
    domain_id        INT AUTO_INCREMENT PRIMARY KEY,
    domain           VARCHAR(1000) NOT NULL UNIQUE,
    uploaded_at      TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE page_url
(
    page_url_id        INT AUTO_INCREMENT PRIMARY KEY,
    client_url         VARCHAR(1000) NULL,
    domain             VARCHAR(1000) NULL,
    page_url           VARCHAR(1000) NULL UNIQUE ,
    published_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

