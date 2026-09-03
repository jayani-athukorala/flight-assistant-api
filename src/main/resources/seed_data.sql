-- ============================================================================
-- FLIGHT ASSISTANT - POSTGRESQL DEMO DATA
-- ============================================================================
-- Seeds:
--   * 15 airports
--   * 16 daily routes for today through the next 60 days
--   * 12 seats for every seeded flight
--
-- This script intentionally does not seed users, passengers, or bookings.
-- Those records should be created through the application.
--
-- The inserts are repeat-safe. Running the script again will not duplicate
-- airports, flight instances, or seats already created by this script.
-- ============================================================================


-- ============================================================================
-- AIRPORTS
-- ============================================================================

INSERT INTO airports (code, name, city, country)
VALUES
    ('ARN', 'Stockholm Arlanda Airport', 'Stockholm', 'Sweden'),
    ('GOT', 'Goteborg Landvetter Airport', 'Gothenburg', 'Sweden'),
    ('LHR', 'Heathrow Airport', 'London', 'United Kingdom'),
    ('CDG', 'Charles de Gaulle Airport', 'Paris', 'France'),
    ('AMS', 'Amsterdam Airport Schiphol', 'Amsterdam', 'Netherlands'),
    ('FRA', 'Frankfurt Airport', 'Frankfurt', 'Germany'),
    ('BER', 'Berlin Brandenburg Airport', 'Berlin', 'Germany'),
    ('MAD', 'Adolfo Suarez Madrid-Barajas Airport', 'Madrid', 'Spain'),
    ('BCN', 'Barcelona-El Prat Airport', 'Barcelona', 'Spain'),
    ('FCO', 'Leonardo da Vinci-Fiumicino Airport', 'Rome', 'Italy'),
    ('CPH', 'Copenhagen Airport', 'Copenhagen', 'Denmark'),
    ('VIE', 'Vienna International Airport', 'Vienna', 'Austria'),
    ('OSL', 'Oslo Airport Gardermoen', 'Oslo', 'Norway'),
    ('HEL', 'Helsinki Airport', 'Helsinki', 'Finland'),
    ('DUB', 'Dublin Airport', 'Dublin', 'Ireland')
ON CONFLICT DO NOTHING;


-- ============================================================================
-- FLIGHTS
-- ============================================================================
-- A flight instance is considered the same when its flight number, origin,
-- destination, and departure time match.

WITH routes (
             flight_number,
             airline,
             origin_code,
             destination_code,
             departure_at,
             duration_minutes
    ) AS (
    VALUES
        ('SK101', 'SAS',        'ARN', 'LHR', TIME '07:30:00', 165),
        ('SK102', 'SAS',        'LHR', 'ARN', TIME '13:00:00', 150),
        ('SK201', 'SAS',        'GOT', 'CDG', TIME '08:15:00', 135),
        ('SK202', 'SAS',        'CDG', 'GOT', TIME '16:30:00', 130),
        ('KL301', 'KLM',        'AMS', 'FCO', TIME '09:00:00', 135),
        ('KL302', 'KLM',        'FCO', 'AMS', TIME '17:30:00', 145),
        ('LH401', 'Lufthansa',  'FRA', 'MAD', TIME '10:20:00', 165),
        ('LH402', 'Lufthansa',  'MAD', 'FRA', TIME '18:10:00', 155),
        ('SK501', 'SAS',        'CPH', 'BER', TIME '11:45:00',  65),
        ('SK502', 'SAS',        'BER', 'CPH', TIME '19:20:00',  70),
        ('DY601', 'Norwegian',  'OSL', 'DUB', TIME '07:10:00', 140),
        ('DY602', 'Norwegian',  'DUB', 'OSL', TIME '14:30:00', 130),
        ('AY701', 'Finnair',    'HEL', 'VIE', TIME '09:40:00', 150),
        ('AY702', 'Finnair',    'VIE', 'HEL', TIME '16:20:00', 145),
        ('VY801', 'Vueling',    'BCN', 'FCO', TIME '12:15:00', 110),
        ('VY802', 'Vueling',    'FCO', 'BCN', TIME '18:45:00', 115)
),
     flight_candidates AS (
         SELECT
             routes.flight_number,
             routes.airline,
             origin_airport.id AS origin_airport_id,
             destination_airport.id AS destination_airport_id,
             generated_dates.flight_date::date + routes.departure_at AS departure_time,
             generated_dates.flight_date::date + routes.departure_at
                 + routes.duration_minutes * INTERVAL '1 minute' AS arrival_time
         FROM generate_series(
                              CURRENT_DATE,
                              CURRENT_DATE + 60,
                              INTERVAL '1 day'
              ) AS generated_dates(flight_date)
                  CROSS JOIN routes
                  JOIN airports AS origin_airport
                       ON origin_airport.code = routes.origin_code
                  JOIN airports AS destination_airport
                       ON destination_airport.code = routes.destination_code
     )
INSERT INTO flight (
    flight_number,
    airline,
    origin_airport_id,
    destination_airport_id,
    departure_time,
    arrival_time,
    status
)
SELECT
    candidate.flight_number,
    candidate.airline,
    candidate.origin_airport_id,
    candidate.destination_airport_id,
    candidate.departure_time,
    candidate.arrival_time,
    'SCHEDULED'
FROM flight_candidates AS candidate
WHERE NOT EXISTS (
    SELECT 1
    FROM flight AS existing
    WHERE existing.flight_number = candidate.flight_number
      AND existing.origin_airport_id = candidate.origin_airport_id
      AND existing.destination_airport_id = candidate.destination_airport_id
      AND existing.departure_time = candidate.departure_time
);


-- ============================================================================
-- FLIGHT SEATS
-- ============================================================================
-- Prices are stored as NUMERIC values and become each flight's starting price.

WITH seat_templates (seat_number, seat_class, price) AS (
    VALUES
        ('1A',  'FIRST_CLASS',      999.99::numeric(10, 2)),
        ('2A',  'BUSINESS',         599.99::numeric(10, 2)),
        ('2B',  'BUSINESS',         599.99::numeric(10, 2)),
        ('5A',  'PREMIUM_ECONOMY',  299.99::numeric(10, 2)),
        ('5B',  'PREMIUM_ECONOMY',  299.99::numeric(10, 2)),
        ('10A', 'ECONOMY',          199.99::numeric(10, 2)),
        ('10B', 'ECONOMY',          199.99::numeric(10, 2)),
        ('11A', 'ECONOMY',          199.99::numeric(10, 2)),
        ('11B', 'ECONOMY',          199.99::numeric(10, 2)),
        ('12A', 'ECONOMY',          179.99::numeric(10, 2)),
        ('12B', 'ECONOMY',          179.99::numeric(10, 2)),
        ('12C', 'ECONOMY',          169.99::numeric(10, 2))
),
     seeded_flight_numbers (flight_number) AS (
         VALUES
             ('SK101'), ('SK102'), ('SK201'), ('SK202'),
             ('KL301'), ('KL302'), ('LH401'), ('LH402'),
             ('SK501'), ('SK502'), ('DY601'), ('DY602'),
             ('AY701'), ('AY702'), ('VY801'), ('VY802')
     )
INSERT INTO flight_seat (
    seat_number,
    seat_class,
    price,
    flight_id
)
SELECT
    template.seat_number,
    template.seat_class,
    template.price,
    seeded_flight.id
FROM flight AS seeded_flight
         JOIN seeded_flight_numbers
              ON seeded_flight_numbers.flight_number = seeded_flight.flight_number
         CROSS JOIN seat_templates AS template
WHERE seeded_flight.departure_time::date BETWEEN CURRENT_DATE AND CURRENT_DATE + 60
  AND NOT EXISTS (
    SELECT 1
    FROM flight_seat AS existing_seat
    WHERE existing_seat.flight_id = seeded_flight.id
      AND existing_seat.seat_number = template.seat_number
);


-- ============================================================================
-- OPTIONAL VERIFICATION
-- ============================================================================

SELECT 'Airports' AS entity, COUNT(*) AS total
FROM airports
UNION ALL
SELECT 'Future seeded flights', COUNT(*)
FROM flight
WHERE departure_time::date BETWEEN CURRENT_DATE AND CURRENT_DATE + 60
UNION ALL
SELECT 'Seats on future flights', COUNT(*)
FROM flight_seat AS seat
         JOIN flight ON flight.id = seat.flight_id
WHERE flight.departure_time::date BETWEEN CURRENT_DATE AND CURRENT_DATE + 60;
