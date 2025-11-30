-- Clean up existing data that represents fresh install default values
-- Set led_count and is_matrix to NULL where led_count = 30 (default value indicating fresh install)
UPDATE device SET led_count = NULL, is_matrix = NULL WHERE led_count = 30;
