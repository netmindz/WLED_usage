CREATE TABLE device
(
    id VARCHAR(255) NOT NULL,
    version   VARCHAR(255) NOT NULL,
    `release` VARCHAR(255) NOT NULL,
    chip      VARCHAR(255) NOT NULL,
    led_count INT          NOT NULL,
    is_matrix BIT(1)       NOT NULL,
    CONSTRAINT pk_device PRIMARY KEY (id)
);