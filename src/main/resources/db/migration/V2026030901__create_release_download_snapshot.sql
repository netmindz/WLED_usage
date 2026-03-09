CREATE TABLE release_download_snapshot (
    id BIGINT AUTO_INCREMENT NOT NULL,
    repo_name VARCHAR(255) NOT NULL,
    tag_name VARCHAR(255) NOT NULL,
    asset_name VARCHAR(255) NOT NULL,
    download_count BIGINT NOT NULL,
    delta BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    created DATETIME,
    CONSTRAINT pk_release_download_snapshot PRIMARY KEY (id),
    CONSTRAINT uq_release_download_snapshot UNIQUE (repo_name, tag_name, asset_name, snapshot_date)
);

CREATE INDEX idx_rds_repo_tag_asset ON release_download_snapshot (repo_name, tag_name, asset_name);
CREATE INDEX idx_rds_snapshot_date ON release_download_snapshot (snapshot_date);
