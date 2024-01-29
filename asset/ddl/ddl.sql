CREATE TABLE customer
(
    customer_id        INT AUTO_INCREMENT PRIMARY KEY,
    customer_name           VARCHAR(64) NULL,
    page_url                VARCHAR(1000) NULL
);

CREATE TABLE domain
(
    domain_id        INT AUTO_INCREMENT PRIMARY KEY,
    domain           VARCHAR(500) NOT NULL,
    uploaded_at      TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE domain ADD UNIQUE (domain);


CREATE TABLE registered_domain
(
    customer_id      INT NOT NULL,
    domain_id        INT NOT NULL,
    page_url         VARCHAR(1000) NULL,
    domain           VARCHAR(500)  NULL,
    url              VARCHAR(1000) NULL,
    uploaded_at      TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (customer_id, domain_id, uploaded_at)
);

