-- ============================================================================
-- SKYROUTE FLIGHT ASSISTANT SEED DATA
-- PostgreSQL | 9-24 September 2026
--
-- Creates:
--   * 15 airports
--   * 4 outbound and 4 return flights per day
--   * 128 flights across 16 days
--   * 12 seats per flight (1,536 seats)
--
-- The inserts are repeat-safe. Existing matching airports, flights and seats
-- are preserved.
-- ============================================================================

BEGIN;

-- ----------------------------------------------------------------------------
-- 1. Airports
-- ----------------------------------------------------------------------------

INSERT INTO airports (code, name, city, country)
VALUES
    ('ARN', 'Stockholm Arlanda Airport', 'Stockholm', 'Sweden'),
    ('GOT', 'Goteborg Landvetter Airport', 'Gothenburg', 'Sweden'),
    ('CDG', 'Charles de Gaulle Airport', 'Paris', 'France'),
    ('LHR', 'Heathrow Airport', 'London', 'United Kingdom'),
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

-- ----------------------------------------------------------------------------
-- 2. Daily flights
--
-- Every rotation generates exactly one outbound and one return service.
-- Four rotations therefore generate four outbound and four return flights
-- every day.
-- ----------------------------------------------------------------------------

WITH daily_rotations (
                      outbound_flight_number,
                      return_flight_number,
                      airline,
                      origin_code,
                      destination_code,
                      outbound_departure,
                      return_departure,
                      outbound_duration_minutes,
                      return_duration_minutes
    ) AS (
    VALUES
        ('SK201',  'SK202',  'SAS',         'GOT', 'CDG', TIME '08:15', TIME '16:30', 135, 130),
        ('AF1550', 'AF1551', 'Air France',  'GOT', 'CDG', TIME '13:45', TIME '19:15', 140, 135),
        ('DY421',  'DY422',  'Norwegian',   'GOT', 'ARN', TIME '06:45', TIME '18:10',  60,  60),
        ('AF1450', 'AF1451', 'Air France',  'ARN', 'CDG', TIME '09:20', TIME '15:40', 160, 155)
),
     flight_candidates AS (
         SELECT
             service.flight_number,
             rotation.airline,
             origin.id AS origin_airport_id,
             destination.id AS destination_airport_id,
             dates.flight_date::date + service.departure_at AS departure_time,
             dates.flight_date::date
                 + service.departure_at
                 + service.duration_minutes * INTERVAL '1 minute' AS arrival_time
         FROM generate_series(
                      DATE '2026-09-09',
                      DATE '2026-09-24',
                      INTERVAL '1 day'
              ) AS dates(flight_date)
                  CROSS JOIN daily_rotations AS rotation
                  CROSS JOIN LATERAL (
             VALUES
                 (
                     rotation.outbound_flight_number,
                     rotation.origin_code,
                     rotation.destination_code,
                     rotation.outbound_departure,
                     rotation.outbound_duration_minutes
                 ),
                 (
                     rotation.return_flight_number,
                     rotation.destination_code,
                     rotation.origin_code,
                     rotation.return_departure,
                     rotation.return_duration_minutes
                 )
             ) AS service(
                          flight_number,
                          origin_code,
                          destination_code,
                          departure_at,
                          duration_minutes
             )
                  JOIN airports AS origin
                       ON origin.code = service.origin_code
                  JOIN airports AS destination
                       ON destination.code = service.destination_code
     ),
     inserted_flights AS (
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
             )
             RETURNING id
     )
SELECT COUNT(*) AS newly_inserted_flights
FROM inserted_flights;

-- ----------------------------------------------------------------------------
-- 3. Flight seats and prices
--
-- Every seeded flight receives 12 seats:
--   1 first-class, 2 business, 2 premium-economy and 7 economy seats.
-- Prices vary by route, date, cabin and seat position.
-- ----------------------------------------------------------------------------

WITH service_fares (flight_number, base_fare) AS (
    VALUES
        ('SK201',   849.00::numeric),
        ('SK202',   799.00::numeric),
        ('AF1550',  779.00::numeric),
        ('AF1551',  749.00::numeric),
        ('DY421',   499.00::numeric),
        ('DY422',   479.00::numeric),
        ('AF1450', 1099.00::numeric),
        ('AF1451', 1049.00::numeric)
),
     seat_templates (seat_number, seat_class, multiplier, adjustment) AS (
         VALUES
             ('1A',  'FIRST_CLASS',      3.20::numeric, 500.00::numeric),
             ('2A',  'BUSINESS',         2.10::numeric, 250.00::numeric),
             ('2B',  'BUSINESS',         2.10::numeric, 250.00::numeric),
             ('5A',  'PREMIUM_ECONOMY',  1.45::numeric, 120.00::numeric),
             ('5B',  'PREMIUM_ECONOMY',  1.45::numeric, 120.00::numeric),
             ('10A', 'ECONOMY',          1.00::numeric,  80.00::numeric),
             ('10B', 'ECONOMY',          1.00::numeric,  80.00::numeric),
             ('11A', 'ECONOMY',          1.00::numeric,  50.00::numeric),
             ('11B', 'ECONOMY',          1.00::numeric,  50.00::numeric),
             ('12A', 'ECONOMY',          1.00::numeric,  20.00::numeric),
             ('12B', 'ECONOMY',          1.00::numeric,  20.00::numeric),
             ('12C', 'ECONOMY',          1.00::numeric,   0.00::numeric)
     ),
     target_flights AS (
         SELECT
             scheduled.id,
             fare.base_fare
                 + ((scheduled.departure_time::date - DATE '2026-09-09') % 5)
                 * 35.00 AS economy_fare
         FROM flight AS scheduled
                  JOIN service_fares AS fare
                       ON fare.flight_number = scheduled.flight_number
         WHERE scheduled.departure_time::date
                   BETWEEN DATE '2026-09-09' AND DATE '2026-09-24'
     )
INSERT INTO flight_seat (
    seat_number,
    seat_class,
    price,
    flight_id
)
SELECT
    seat.seat_number,
    seat.seat_class,
    ROUND(target.economy_fare * seat.multiplier + seat.adjustment, 2),
    target.id
FROM target_flights AS target
         CROSS JOIN seat_templates AS seat
WHERE NOT EXISTS (
    SELECT 1
    FROM flight_seat AS existing
    WHERE existing.flight_id = target.id
      AND existing.seat_number = seat.seat_number
);

COMMIT;

-- ----------------------------------------------------------------------------
-- 4. Verification
-- ----------------------------------------------------------------------------

-- Expected: 128 flights and 1,536 seats.
SELECT
    'Seeded flights (expected 128)' AS entity,
    COUNT(*) AS total
FROM flight
WHERE flight_number IN (
                        'SK201', 'SK202', 'AF1550', 'AF1551',
                        'DY421', 'DY422', 'AF1450', 'AF1451'
    )
  AND departure_time::date BETWEEN DATE '2026-09-09' AND DATE '2026-09-24'

UNION ALL

SELECT
    'Seeded seats (expected 1536)',
    COUNT(*)
FROM flight_seat AS seat
         JOIN flight AS scheduled
              ON scheduled.id = seat.flight_id
WHERE scheduled.flight_number IN (
                                  'SK201', 'SK202', 'AF1550', 'AF1551',
                                  'DY421', 'DY422', 'AF1450', 'AF1451'
    )
  AND scheduled.departure_time::date
    BETWEEN DATE '2026-09-09' AND DATE '2026-09-24';

-- Expected for every date: 4 outbound and 4 return flights.
SELECT
    departure_time::date AS flight_date,
    COUNT(*) FILTER (
        WHERE flight_number IN ('SK201', 'AF1550', 'DY421', 'AF1450')
        ) AS outbound_flights,
    COUNT(*) FILTER (
        WHERE flight_number IN ('SK202', 'AF1551', 'DY422', 'AF1451')
        ) AS return_flights,
    COUNT(*) AS total_flights
FROM flight
WHERE flight_number IN (
                        'SK201', 'SK202', 'AF1550', 'AF1551',
                        'DY421', 'DY422', 'AF1450', 'AF1451'
    )
  AND departure_time::date BETWEEN DATE '2026-09-09' AND DATE '2026-09-24'
GROUP BY departure_time::date
ORDER BY flight_date;

-- Detailed daily schedule for manual checking.
SELECT
    scheduled.departure_time::date AS flight_date,
    scheduled.flight_number,
    scheduled.airline,
    origin.code AS origin,
    destination.code AS destination,
    scheduled.departure_time,
    scheduled.arrival_time,
    scheduled.status,
    COUNT(seat.id) AS seat_count
FROM flight AS scheduled
         JOIN airports AS origin
              ON origin.id = scheduled.origin_airport_id
         JOIN airports AS destination
              ON destination.id = scheduled.destination_airport_id
         LEFT JOIN flight_seat AS seat
                   ON seat.flight_id = scheduled.id
WHERE scheduled.flight_number IN (
                                  'SK201', 'SK202', 'AF1550', 'AF1551',
                                  'DY421', 'DY422', 'AF1450', 'AF1451'
    )
  AND scheduled.departure_time::date BETWEEN DATE '2026-09-09' AND DATE '2026-09-24'
GROUP BY
    scheduled.id,
    scheduled.departure_time::date,
    scheduled.flight_number,
    scheduled.airline,
    origin.code,
    destination.code,
    scheduled.departure_time,
    scheduled.arrival_time,
    scheduled.status
ORDER BY scheduled.departure_time;
