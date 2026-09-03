-- ============================================================
-- FLIGHT BOOKING APPLICATION - MYSQL 8 SEED DATA
-- ============================================================
-- Seed window: 2026-09-02 through 2026-09-16 (inclusive)
-- Intended for an empty development database.
--
-- Assumptions matching the current entities:
--   flight.origin_airport_id      -> airports.id
--   flight.destination_airport_id -> airports.id
--   booking.cancelled_at          is nullable
--   booking.archived_at           is nullable
--   BookingSeat rows are retained after cancellation
--   Seat availability is derived from active booking status
-- ============================================================


-- ============================================================
-- AIRPORTS
-- ============================================================

INSERT IGNORE INTO airports (code, name, city, country)
VALUES
    ('ARN', 'Stockholm Arlanda Airport', 'Stockholm', 'Sweden'),
    ('GOT', 'Göteborg Landvetter Airport', 'Gothenburg', 'Sweden'),
    ('LHR', 'Heathrow Airport', 'London', 'United Kingdom'),
    ('CDG', 'Charles de Gaulle Airport', 'Paris', 'France'),
    ('AMS', 'Amsterdam Airport Schiphol', 'Amsterdam', 'Netherlands'),
    ('FRA', 'Frankfurt Airport', 'Frankfurt', 'Germany'),
    ('BER', 'Berlin Brandenburg Airport', 'Berlin', 'Germany'),
    ('MAD', 'Adolfo Suárez Madrid-Barajas Airport', 'Madrid', 'Spain'),
    ('BCN', 'Barcelona-El Prat Airport', 'Barcelona', 'Spain'),
    ('FCO', 'Leonardo da Vinci-Fiumicino Airport', 'Rome', 'Italy'),
    ('CPH', 'Copenhagen Airport', 'Copenhagen', 'Denmark'),
    ('VIE', 'Vienna International Airport', 'Vienna', 'Austria'),
    ('OSL', 'Oslo Airport Gardermoen', 'Oslo', 'Norway'),
    ('HEL', 'Helsinki Airport', 'Helsinki', 'Finland'),
    ('DUB', 'Dublin Airport', 'Dublin', 'Ireland');


-- ============================================================
-- FLIGHTS
-- ============================================================
-- Ten flights per day: five outbound/return route pairs.
-- 15 dates x 10 routes = 150 scheduled flight instances.
-- Flight numbers intentionally repeat on different dates.
-- ============================================================

INSERT IGNORE INTO flight
(
    flight_number,
    airline,
    origin_airport_id,
    destination_airport_id,
    departure_time,
    arrival_time,
    status
)
SELECT
    routes.flight_number,
    routes.airline,
    origin_airport.id,
    destination_airport.id,
    TIMESTAMP(seed_dates.flight_date, routes.departure_time),
    DATE_ADD(
            TIMESTAMP(seed_dates.flight_date, routes.departure_time),
            INTERVAL routes.duration_minutes MINUTE
    ),
    'SCHEDULED'
FROM
    (
        SELECT DATE('2026-09-02') AS flight_date
        UNION ALL SELECT DATE('2026-09-03')
        UNION ALL SELECT DATE('2026-09-04')
        UNION ALL SELECT DATE('2026-09-05')
        UNION ALL SELECT DATE('2026-09-06')
        UNION ALL SELECT DATE('2026-09-07')
        UNION ALL SELECT DATE('2026-09-08')
        UNION ALL SELECT DATE('2026-09-09')
        UNION ALL SELECT DATE('2026-09-10')
        UNION ALL SELECT DATE('2026-09-11')
        UNION ALL SELECT DATE('2026-09-12')
        UNION ALL SELECT DATE('2026-09-13')
        UNION ALL SELECT DATE('2026-09-14')
        UNION ALL SELECT DATE('2026-09-15')
        UNION ALL SELECT DATE('2026-09-16')
    ) seed_dates
        CROSS JOIN
    (
        SELECT 'SK101' AS flight_number, 'SAS' AS airline,
               'ARN' AS origin_code, 'LHR' AS destination_code,
               TIME('07:30:00') AS departure_time, 165 AS duration_minutes
        UNION ALL
        SELECT 'SK102', 'SAS', 'LHR', 'ARN', TIME('13:00:00'), 150
        UNION ALL
        SELECT 'SK201', 'SAS', 'GOT', 'CDG', TIME('08:15:00'), 135
        UNION ALL
        SELECT 'SK202', 'SAS', 'CDG', 'GOT', TIME('16:30:00'), 130
        UNION ALL
        SELECT 'KL301', 'KLM', 'AMS', 'FCO', TIME('09:00:00'), 135
        UNION ALL
        SELECT 'KL302', 'KLM', 'FCO', 'AMS', TIME('17:30:00'), 145
        UNION ALL
        SELECT 'LH401', 'Lufthansa', 'FRA', 'MAD', TIME('10:20:00'), 165
        UNION ALL
        SELECT 'LH402', 'Lufthansa', 'MAD', 'FRA', TIME('18:10:00'), 155
        UNION ALL
        SELECT 'SK501', 'SAS', 'CPH', 'BER', TIME('11:45:00'), 65
        UNION ALL
        SELECT 'SK502', 'SAS', 'BER', 'CPH', TIME('19:20:00'), 70
    ) routes
        JOIN airports origin_airport
             ON origin_airport.code = routes.origin_code
        JOIN airports destination_airport
             ON destination_airport.code = routes.destination_code;


-- Additional routes for broader airport-search demonstrations.

INSERT IGNORE INTO flight
(
    flight_number,
    airline,
    origin_airport_id,
    destination_airport_id,
    departure_time,
    arrival_time,
    status
)
SELECT
    routes.flight_number,
    routes.airline,
    origin_airport.id,
    destination_airport.id,
    TIMESTAMP(seed_dates.flight_date, routes.departure_time),
    DATE_ADD(
            TIMESTAMP(seed_dates.flight_date, routes.departure_time),
            INTERVAL routes.duration_minutes MINUTE
    ),
    'SCHEDULED'
FROM
    (
        SELECT DATE('2026-09-03') AS flight_date
        UNION ALL SELECT DATE('2026-09-05')
        UNION ALL SELECT DATE('2026-09-07')
        UNION ALL SELECT DATE('2026-09-09')
        UNION ALL SELECT DATE('2026-09-11')
        UNION ALL SELECT DATE('2026-09-13')
        UNION ALL SELECT DATE('2026-09-15')
    ) seed_dates
        CROSS JOIN
    (
        SELECT 'DY601' AS flight_number, 'Norwegian' AS airline,
               'OSL' AS origin_code, 'DUB' AS destination_code,
               TIME('07:10:00') AS departure_time, 140 AS duration_minutes
        UNION ALL
        SELECT 'DY602', 'Norwegian', 'DUB', 'OSL', TIME('14:30:00'), 130
        UNION ALL
        SELECT 'AY701', 'Finnair', 'HEL', 'VIE', TIME('09:40:00'), 150
        UNION ALL
        SELECT 'AY702', 'Finnair', 'VIE', 'HEL', TIME('16:20:00'), 145
        UNION ALL
        SELECT 'VY801', 'Vueling', 'BCN', 'FCO', TIME('12:15:00'), 110
        UNION ALL
        SELECT 'VY802', 'Vueling', 'FCO', 'BCN', TIME('18:45:00'), 115
    ) routes
        JOIN airports origin_airport
             ON origin_airport.code = routes.origin_code
        JOIN airports destination_airport
             ON destination_airport.code = routes.destination_code;


-- ============================================================
-- FLIGHT SEATS
-- ============================================================
-- 12 seats per flight.
-- Prices come from the database and are trusted by booking logic.
-- ============================================================

INSERT IGNORE INTO flight_seat
(
    seat_number,
    seat_class,
    price,
    flight_id
)
SELECT
    seat_template.seat_number,
    seat_template.seat_class,
    seat_template.price,
    flight.id
FROM flight
         CROSS JOIN
     (
         SELECT '1A' AS seat_number, 'FIRST_CLASS' AS seat_class, 999.99 AS price
         UNION ALL SELECT '2A', 'BUSINESS', 599.99
         UNION ALL SELECT '2B', 'BUSINESS', 599.99
         UNION ALL SELECT '5A', 'PREMIUM_ECONOMY', 299.99
         UNION ALL SELECT '5B', 'PREMIUM_ECONOMY', 299.99
         UNION ALL SELECT '10A', 'ECONOMY', 199.99
         UNION ALL SELECT '10B', 'ECONOMY', 199.99
         UNION ALL SELECT '11A', 'ECONOMY', 199.99
         UNION ALL SELECT '11B', 'ECONOMY', 199.99
         UNION ALL SELECT '12A', 'ECONOMY', 179.99
         UNION ALL SELECT '12B', 'ECONOMY', 179.99
         UNION ALL SELECT '12C', 'ECONOMY', 169.99
     ) seat_template;


-- ============================================================
-- USERS
-- ============================================================
-- These authenticated users are expected to exist before this
-- file runs. Adapt this section to your actual User columns and
-- password encoding if they are not created elsewhere:
--
--   john@test.com
--   jane@test.com
--   admin@test.com
-- ============================================================


-- ============================================================
-- PASSENGERS
-- ============================================================

INSERT IGNORE INTO passenger
(
    first_name,
    last_name,
    passport_number,
    email,
    created_at,
    updated_at
)
VALUES
    ('John', 'Doe', 'PASS10001', 'john@test.com',
     '2026-08-29 09:00:00', '2026-08-29 09:00:00'),
    ('Jane', 'Doe', 'PASS10002', 'jane.doe@test.com',
     '2026-08-29 09:05:00', '2026-08-29 09:05:00'),
    ('Emma', 'Smith', 'PASS20001', 'jane@test.com',
     '2026-08-29 09:10:00', '2026-08-29 09:10:00'),
    ('Oliver', 'Smith', 'PASS20002', 'oliver@test.com',
     '2026-08-29 09:15:00', '2026-08-29 09:15:00'),
    ('Mia', 'Andersson', 'PASS30001', 'mia@test.com',
     '2026-08-29 09:20:00', '2026-08-29 09:20:00');


-- ============================================================
-- BOOKINGS
-- ============================================================
-- FB-SEED001: confirmed one-way
-- FB-SEED002: confirmed round trip with two passengers
-- FB-SEED003: cancelled, visible in Cancelled tab
-- FB-SEED004: cancelled and archived
-- ============================================================

INSERT IGNORE INTO booking
(
    booking_reference,
    booking_date,
    created_at,
    updated_at,
    cancelled_at,
    archived_at,
    status,
    trip_type,
    outbound_flight_id,
    return_flight_id,
    total_price,
    user_id
)
SELECT
    'FB-SEED001',
    '2026-09-02 08:00:00',
    '2026-09-02 08:00:00',
    '2026-09-02 08:00:00',
    NULL,
    NULL,
    'CONFIRMED',
    'ONE_WAY',
    outbound.id,
    NULL,
    199.99,
    app_user.id
FROM flight outbound
         JOIN airports origin_airport
              ON origin_airport.id = outbound.origin_airport_id
         JOIN airports destination_airport
              ON destination_airport.id = outbound.destination_airport_id
         JOIN users app_user
              ON app_user.email = 'john@test.com'
WHERE outbound.flight_number = 'SK101'
  AND origin_airport.code = 'ARN'
  AND destination_airport.code = 'LHR'
  AND outbound.departure_time = '2026-09-04 07:30:00';


INSERT IGNORE INTO booking
(
    booking_reference,
    booking_date,
    created_at,
    updated_at,
    cancelled_at,
    archived_at,
    status,
    trip_type,
    outbound_flight_id,
    return_flight_id,
    total_price,
    user_id
)
SELECT
    'FB-SEED002',
    '2026-09-02 09:15:00',
    '2026-09-02 09:15:00',
    '2026-09-02 09:15:00',
    NULL,
    NULL,
    'CONFIRMED',
    'ROUND_TRIP',
    outbound.id,
    return_flight.id,
    2399.96,
    app_user.id
FROM flight outbound
         JOIN airports outbound_origin
              ON outbound_origin.id = outbound.origin_airport_id
         JOIN airports outbound_destination
              ON outbound_destination.id = outbound.destination_airport_id
         JOIN flight return_flight
              ON return_flight.flight_number = 'SK202'
                  AND return_flight.departure_time = '2026-09-12 16:30:00'
         JOIN users app_user
              ON app_user.email = 'jane@test.com'
WHERE outbound.flight_number = 'SK201'
  AND outbound_origin.code = 'GOT'
  AND outbound_destination.code = 'CDG'
  AND outbound.departure_time = '2026-09-08 08:15:00';


INSERT IGNORE INTO booking
(
    booking_reference,
    booking_date,
    created_at,
    updated_at,
    cancelled_at,
    archived_at,
    status,
    trip_type,
    outbound_flight_id,
    return_flight_id,
    total_price,
    user_id
)
SELECT
    'FB-SEED003',
    '2026-09-01 14:00:00',
    '2026-09-01 14:00:00',
    '2026-09-02 10:00:00',
    '2026-09-02 10:00:00',
    NULL,
    'CANCELLED',
    'ONE_WAY',
    outbound.id,
    NULL,
    599.99,
    app_user.id
FROM flight outbound
         JOIN airports origin_airport
              ON origin_airport.id = outbound.origin_airport_id
         JOIN airports destination_airport
              ON destination_airport.id = outbound.destination_airport_id
         JOIN users app_user
              ON app_user.email = 'john@test.com'
WHERE outbound.flight_number = 'LH401'
  AND origin_airport.code = 'FRA'
  AND destination_airport.code = 'MAD'
  AND outbound.departure_time = '2026-09-10 10:20:00';


INSERT IGNORE INTO booking
(
    booking_reference,
    booking_date,
    created_at,
    updated_at,
    cancelled_at,
    archived_at,
    status,
    trip_type,
    outbound_flight_id,
    return_flight_id,
    total_price,
    user_id
)
SELECT
    'FB-SEED004',
    '2026-08-30 12:00:00',
    '2026-08-30 12:00:00',
    '2026-09-01 09:00:00',
    '2026-08-31 09:00:00',
    '2026-09-01 09:00:00',
    'CANCELLED',
    'ONE_WAY',
    outbound.id,
    NULL,
    179.99,
    app_user.id
FROM flight outbound
         JOIN airports origin_airport
              ON origin_airport.id = outbound.origin_airport_id
         JOIN airports destination_airport
              ON destination_airport.id = outbound.destination_airport_id
         JOIN users app_user
              ON app_user.email = 'jane@test.com'
WHERE outbound.flight_number = 'KL301'
  AND origin_airport.code = 'AMS'
  AND destination_airport.code = 'FCO'
  AND outbound.departure_time = '2026-09-06 09:00:00';


-- ============================================================
-- BOOKING <-> PASSENGERS
-- ============================================================

INSERT IGNORE INTO booking_passengers (booking_id, passenger_id)
SELECT booking.id, passenger.id
FROM booking
         JOIN passenger ON passenger.passport_number = 'PASS10001'
WHERE booking.booking_reference = 'FB-SEED001';

INSERT IGNORE INTO booking_passengers (booking_id, passenger_id)
SELECT booking.id, passenger.id
FROM booking
         JOIN passenger
              ON passenger.passport_number IN ('PASS20001', 'PASS20002')
WHERE booking.booking_reference = 'FB-SEED002';

INSERT IGNORE INTO booking_passengers (booking_id, passenger_id)
SELECT booking.id, passenger.id
FROM booking
         JOIN passenger ON passenger.passport_number = 'PASS10002'
WHERE booking.booking_reference = 'FB-SEED003';

INSERT IGNORE INTO booking_passengers (booking_id, passenger_id)
SELECT booking.id, passenger.id
FROM booking
         JOIN passenger ON passenger.passport_number = 'PASS30001'
WHERE booking.booking_reference = 'FB-SEED004';


-- ============================================================
-- BOOKING SEATS
-- ============================================================
-- Historical BookingSeat rows are intentionally kept for the
-- cancelled and archived bookings. Availability queries must
-- only treat PENDING/CONFIRMED bookings as active reservations.
-- ============================================================

-- Confirmed one-way: John -> 10A
INSERT IGNORE INTO booking_seat
    (booking_id, seat_id, passenger_id, created_at, updated_at)
SELECT booking.id, flight_seat.id, passenger.id,
       booking.created_at, booking.updated_at
FROM booking
         JOIN passenger ON passenger.passport_number = 'PASS10001'
         JOIN flight_seat
              ON flight_seat.flight_id = booking.outbound_flight_id
                  AND flight_seat.seat_number = '10A'
WHERE booking.booking_reference = 'FB-SEED001';

-- Confirmed round trip: Emma -> 2A outbound and return
INSERT IGNORE INTO booking_seat
    (booking_id, seat_id, passenger_id, created_at, updated_at)
SELECT booking.id, flight_seat.id, passenger.id,
       booking.created_at, booking.updated_at
FROM booking
         JOIN passenger ON passenger.passport_number = 'PASS20001'
         JOIN flight_seat
              ON flight_seat.flight_id IN (
                                           booking.outbound_flight_id,
                                           booking.return_flight_id
                  )
                  AND flight_seat.seat_number = '2A'
WHERE booking.booking_reference = 'FB-SEED002';

-- Confirmed round trip: Oliver -> 2B outbound and return
INSERT IGNORE INTO booking_seat
    (booking_id, seat_id, passenger_id, created_at, updated_at)
SELECT booking.id, flight_seat.id, passenger.id,
       booking.created_at, booking.updated_at
FROM booking
         JOIN passenger ON passenger.passport_number = 'PASS20002'
         JOIN flight_seat
              ON flight_seat.flight_id IN (
                                           booking.outbound_flight_id,
                                           booking.return_flight_id
                  )
                  AND flight_seat.seat_number = '2B'
WHERE booking.booking_reference = 'FB-SEED002';

-- Cancelled booking history: Jane -> 2A
INSERT IGNORE INTO booking_seat
    (booking_id, seat_id, passenger_id, created_at, updated_at)
SELECT booking.id, flight_seat.id, passenger.id,
       booking.created_at, booking.updated_at
FROM booking
         JOIN passenger ON passenger.passport_number = 'PASS10002'
         JOIN flight_seat
              ON flight_seat.flight_id = booking.outbound_flight_id
                  AND flight_seat.seat_number = '2A'
WHERE booking.booking_reference = 'FB-SEED003';

-- Archived booking history: Mia -> 12A
INSERT IGNORE INTO booking_seat
    (booking_id, seat_id, passenger_id, created_at, updated_at)
SELECT booking.id, flight_seat.id, passenger.id,
       booking.created_at, booking.updated_at
FROM booking
         JOIN passenger ON passenger.passport_number = 'PASS30001'
         JOIN flight_seat
              ON flight_seat.flight_id = booking.outbound_flight_id
                  AND flight_seat.seat_number = '12A'
WHERE booking.booking_reference = 'FB-SEED004';


-- ============================================================
-- VERIFICATION
-- ============================================================

SELECT 'Airports' AS entity, COUNT(*) AS total FROM airports
UNION ALL
SELECT 'Flights', COUNT(*) FROM flight
UNION ALL
SELECT 'Flight Seats', COUNT(*) FROM flight_seat
UNION ALL
SELECT 'Passengers', COUNT(*) FROM passenger
UNION ALL
SELECT 'Bookings', COUNT(*) FROM booking
UNION ALL
SELECT 'Booking Seats', COUNT(*) FROM booking_seat;

-- Expected on an empty database with the required users:
-- Airports       = 15
-- Flights        = 192  (150 daily + 42 additional)
-- Flight Seats   = 2304 (192 x 12)
-- Passengers     = 5
-- Bookings       = 4
-- Booking Seats  = 7
