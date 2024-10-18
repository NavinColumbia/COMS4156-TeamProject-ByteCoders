--DELETE FROM USERs;
--DELETE FROM USER_TYPES;
INSERT INTO user_types (id, name, description, can_access_all_records) VALUES
('1', 'USER', 'Regular user', false),
('2', 'ADMIN', 'Admin user', true);