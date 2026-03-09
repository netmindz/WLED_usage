CREATE TABLE repo_history
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    device_id           VARCHAR(255)          NOT NULL,
    repo                VARCHAR(255)          NOT NULL,
    device_last_update  DATETIME,
    created             DATETIME,
    CONSTRAINT pk_repo_history PRIMARY KEY (id),
    CONSTRAINT fk_repo_history_device FOREIGN KEY (device_id) REFERENCES device (id)
);

CREATE INDEX idx_repo_history_device_id ON repo_history (device_id);
