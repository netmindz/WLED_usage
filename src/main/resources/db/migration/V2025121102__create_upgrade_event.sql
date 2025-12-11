CREATE TABLE upgrade_event
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    device_id   VARCHAR(255)          NOT NULL,
    old_version VARCHAR(255)          NOT NULL,
    new_version VARCHAR(255)          NOT NULL,
    created     DATETIME,
    CONSTRAINT pk_upgrade_event PRIMARY KEY (id),
    CONSTRAINT fk_upgrade_event_device FOREIGN KEY (device_id) REFERENCES device (id)
);

CREATE INDEX idx_upgrade_event_device_id ON upgrade_event (device_id);
