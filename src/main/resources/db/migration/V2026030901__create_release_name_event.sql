CREATE TABLE release_name_history
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    device_id           VARCHAR(255)          NOT NULL,
    release_name        VARCHAR(255)          NOT NULL,
    device_last_update  DATETIME,
    created             DATETIME,
    CONSTRAINT pk_release_name_history PRIMARY KEY (id),
    CONSTRAINT fk_release_name_history_device FOREIGN KEY (device_id) REFERENCES device (id)
);

CREATE INDEX idx_release_name_history_device_id ON release_name_history (device_id);
