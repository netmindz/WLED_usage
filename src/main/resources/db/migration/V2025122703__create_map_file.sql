CREATE TABLE map_file
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    version      VARCHAR(255)          NOT NULL,
    release_name VARCHAR(255),
    chip         VARCHAR(255),
    content      MEDIUMTEXT            NOT NULL,
    uploaded_at  DATETIME,
    CONSTRAINT pk_map_file PRIMARY KEY (id),
    CONSTRAINT uk_map_file_version UNIQUE (version)
);

CREATE INDEX idx_map_file_uploaded_at ON map_file (uploaded_at);
