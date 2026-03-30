ALTER TABLE `device`
    -- Change flash_size and psram_size from VARCHAR to INT
    MODIFY COLUMN `flash_size` INT NULL,
    MODIFY COLUMN `psram_size` INT NULL,
    -- Filesystem usage
    ADD COLUMN `fs_used` INT NULL,
    ADD COLUMN `fs_total` INT NULL,
    -- Bus / hardware
    ADD COLUMN `bus_count` INT NULL,
    ADD COLUMN `bus_types` VARCHAR(255) NULL,
    -- Feature flags
    ADD COLUMN `has_rgbw` BIT NULL,
    ADD COLUMN `has_cct` BIT NULL,
    ADD COLUMN `abl_enabled` BIT NULL,
    ADD COLUMN `cct_from_rgb` BIT NULL,
    ADD COLUMN `white_balance_correction` BIT NULL,
    ADD COLUMN `gamma_correction` BIT NULL,
    ADD COLUMN `auto_segments` BIT NULL,
    ADD COLUMN `nightlight_enabled` BIT NULL,
    ADD COLUMN `relay_configured` BIT NULL,
    ADD COLUMN `button_count` INT NULL,
    ADD COLUMN `i2c_configured` BIT NULL,
    ADD COLUMN `spi_configured` BIT NULL,
    ADD COLUMN `ethernet_enabled` BIT NULL,
    ADD COLUMN `hue_enabled` BIT NULL,
    ADD COLUMN `mqtt_enabled` BIT NULL,
    ADD COLUMN `alexa_enabled` BIT NULL,
    ADD COLUMN `wled_sync_send` BIT NULL,
    ADD COLUMN `esp_now_enabled` BIT NULL,
    ADD COLUMN `esp_now_sync` BIT NULL,
    ADD COLUMN `esp_now_remote_count` INT NULL,
    -- Usermods (stored as comma-separated strings)
    ADD COLUMN `usermods` VARCHAR(1024) NULL,
    ADD COLUMN `usermod_ids` VARCHAR(255) NULL;

