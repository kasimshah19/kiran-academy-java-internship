-- Seed Administrator Account (Email: admin@kiranair.com / Password: Admin@123)
-- BCrypt hash for Admin@123: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
INSERT INTO users (full_name, email, password_hash, role, created_at, updated_at)
VALUES (
    'System Administrator',
    'admin@kiranair.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
    'ROLE_ADMIN',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- Seed Sample Flights
INSERT INTO flights (flight_code, airline, source, destination, departure_time, arrival_time, total_seats, available_seats, base_fare, status, created_at, updated_at)
VALUES 
(
    'PN101',
    'Kiran Air',
    'Pune',
    'Delhi',
    CURRENT_TIMESTAMP + INTERVAL '2 days',
    CURRENT_TIMESTAMP + INTERVAL '2 days' + INTERVAL '2 hours 15 minutes',
    20,
    20,
    4000.00,
    'SCHEDULED',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'MB202',
    'Sky Wings',
    'Mumbai',
    'Bengaluru',
    CURRENT_TIMESTAMP + INTERVAL '3 days',
    CURRENT_TIMESTAMP + INTERVAL '3 days' + INTERVAL '1 hour 45 minutes',
    30,
    30,
    3500.00,
    'SCHEDULED',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'PN303',
    'Cloud Jet',
    'Pune',
    'Hyderabad',
    CURRENT_TIMESTAMP + INTERVAL '4 days',
    CURRENT_TIMESTAMP + INTERVAL '4 days' + INTERVAL '1 hour 20 minutes',
    25,
    25,
    3000.00,
    'SCHEDULED',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (flight_code) DO NOTHING;
