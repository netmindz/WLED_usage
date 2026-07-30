CREATE TABLE crash_instance
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    crash_report_id BIGINT                NOT NULL,
    device_id       VARCHAR(255),
    version         VARCHAR(255)          NOT NULL,
    chip            VARCHAR(255),
    country_code    VARCHAR(10),
    reported_at     DATETIME,
    CONSTRAINT pk_crash_instance PRIMARY KEY (id),
    CONSTRAINT fk_crash_instance_crash_report FOREIGN KEY (crash_report_id) REFERENCES crash_report (id),
    CONSTRAINT fk_crash_instance_device FOREIGN KEY (device_id) REFERENCES device (id)
);

CREATE INDEX idx_crash_instance_crash_report_id ON crash_instance (crash_report_id);
CREATE INDEX idx_crash_instance_device_id ON crash_instance (device_id);
CREATE INDEX idx_crash_instance_reported_at ON crash_instance (reported_at);
CREATE INDEX idx_crash_instance_version ON crash_instance (version);
