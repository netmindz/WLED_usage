CREATE TABLE crash_report
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    stack_trace_hash    VARCHAR(64)           NOT NULL,
    raw_stack_trace     TEXT                  NOT NULL,
    decoded_stack_trace TEXT,
    exception_cause     VARCHAR(255),
    first_seen          DATETIME,
    CONSTRAINT pk_crash_report PRIMARY KEY (id),
    CONSTRAINT uk_crash_report_hash UNIQUE (stack_trace_hash)
);

CREATE INDEX idx_crash_report_first_seen ON crash_report (first_seen);
