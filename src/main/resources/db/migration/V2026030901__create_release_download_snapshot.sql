CREATE TABLE release_download_snapshot (
    id BIGINT AUTO_INCREMENT NOT NULL,
    repo_name VARCHAR(255) NOT NULL,
    tag_name VARCHAR(255) NOT NULL,
    asset_name VARCHAR(255) NOT NULL,
    download_count BIGINT NOT NULL,
    delta BIGINT NOT NULL,
    created DATETIME NOT NULL,
    CONSTRAINT pk_release_download_snapshot PRIMARY KEY (id)
);

CREATE INDEX idx_rds_repo_tag_asset ON release_download_snapshot (repo_name, tag_name, asset_name);
CREATE INDEX idx_rds_created ON release_download_snapshot (created);
