ALTER TABLE `device`
    -- Drop old boolean feature flag columns
    DROP COLUMN `has_rgbw`,
    DROP COLUMN `has_cct`,
    DROP COLUMN `abl_enabled`,
    DROP COLUMN `cct_from_rgb`,
    DROP COLUMN `white_balance_correction`,
    DROP COLUMN `gamma_correction`,
    DROP COLUMN `auto_segments`,
    DROP COLUMN `nightlight_enabled`,
    DROP COLUMN `relay_configured`,
    DROP COLUMN `button_count`,
    DROP COLUMN `i2c_configured`,
    DROP COLUMN `spi_configured`,
    DROP COLUMN `ethernet_enabled`,
    DROP COLUMN `hue_enabled`,
    DROP COLUMN `mqtt_enabled`,
    DROP COLUMN `alexa_enabled`,
    DROP COLUMN `wled_sync_send`,
    DROP COLUMN `esp_now_enabled`,
    DROP COLUMN `esp_now_sync`,
    DROP COLUMN `esp_now_remote_count`,
    -- Add new list-based capability columns (stored as comma-separated strings)
    ADD COLUMN `led_features` VARCHAR(512) NULL,
    ADD COLUMN `peripherals` VARCHAR(512) NULL,
    ADD COLUMN `integrations` VARCHAR(512) NULL;

