-- Make led_count and is_matrix columns nullable
-- Fresh installs have default values (ledCount=30, isMatrix=false) which should be stored as NULL
ALTER TABLE device MODIFY COLUMN led_count INT NULL;
ALTER TABLE device MODIFY COLUMN is_matrix BIT(1) NULL;
