-- Create SYSTEM_ADMIN user if not exists
DO $$
DECLARE
    user_id_val bigint;
BEGIN
    -- Check if the user already exists
    IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'dentistdss@gmail.com') THEN
        -- Insert the user
        INSERT INTO users (id, email, password, first_name, last_name, provider, email_verified, enabled, account_non_expired, credentials_non_expired, account_non_locked, created_at, updated_at)
        VALUES (
            nextval('user_id_seq'), 
            'dentistdss@gmail.com', 
            '$2a$10$Eb/KOv0/sly78NCkCxPwjeN95DKXw8wdNlZ23mWeA04CXOHhiGB82', -- Bcrypt hashed password
            'System', 
            'Admin', 
            'LOCAL', 
            TRUE, 
            TRUE, 
            TRUE, 
            TRUE, 
            TRUE, 
            CURRENT_TIMESTAMP, 
            CURRENT_TIMESTAMP
        ) RETURNING id INTO user_id_val;

        -- Assign the SYSTEM_ADMIN role to the user
        INSERT INTO user_roles (user_id, role)
        VALUES (user_id_val, 'SYSTEM_ADMIN');

        RAISE NOTICE 'User dentistdss@gmail.com with SYSTEM_ADMIN role created.';
    ELSE
        RAISE NOTICE 'User dentistdss@gmail.com already exists.';
    END IF;
END;
$$; 