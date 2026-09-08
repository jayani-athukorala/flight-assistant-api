-- ============================================================================
-- FLIGHT ASSISTANT - DAILY MULTI-AIRLINE SCHEDULE, 9-24 SEPTEMBER 2026
-- ============================================================================
-- Eight flights operate every day: four outbound services and their same-day
-- returns. Gothenburg-Paris has morning and evening choices. Every direction
-- has a separate flight number. A number repeats on
-- the following day only after its paired return has completed the rotation.
-- Expected totals: 128 flights and 1,536 seats. This script is repeat-safe.

INSERT INTO airports (code, name, city, country) VALUES
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

WITH daily_services (
                     flight_number, airline, origin_code, destination_code,
                     departure_at, duration_minutes, base_fare
    ) AS (
    VALUES
        ('SK201', 'SAS', 'GOT', 'CDG', TIME '08:15', 135,  849.00::numeric),
        ('SK202', 'SAS', 'CDG', 'GOT', TIME '16:30', 130,  799.00::numeric),
        ('AF1550', 'Air France', 'GOT', 'CDG', TIME '13:45', 140,  779.00::numeric),
        ('AF1551', 'Air France', 'CDG', 'GOT', TIME '19:15', 135,  749.00::numeric),
        ('DY421', 'Norwegian', 'GOT', 'ARN', TIME '06:45',  60,  499.00::numeric),
        ('DY422', 'Norwegian', 'ARN', 'GOT', TIME '18:10',  60,  479.00::numeric),
        ('AF1450', 'Air France', 'ARN', 'CDG', TIME '09:20', 160, 1099.00::numeric),
        ('AF1451', 'Air France', 'CDG', 'ARN', TIME '15:40', 155, 1049.00::numeric)
), flight_candidates AS (
    SELECT
        service.flight_number,
        service.airline,
        origin.id AS origin_airport_id,
        destination.id AS destination_airport_id,
        dates.flight_date::date + service.departure_at AS departure_time,
        dates.flight_date::date + service.departure_at
            + service.duration_minutes * INTERVAL '1 minute' AS arrival_time
    FROM generate_series(
                 DATE '2026-09-09', DATE '2026-09-24', INTERVAL '1 day'
         ) AS dates(flight_date)
             CROSS JOIN daily_services AS service
             JOIN airports AS origin ON origin.code = service.origin_code
             JOIN airports AS destination ON destination.code = service.destination_code
), inserted_flights AS (
    INSERT INTO flight (
                        flight_number, airline, origin_airport_id, destination_airport_id,
                        departure_time, arrival_time, status
        )
        SELECT
            candidate.flight_number, candidate.airline,
            candidate.origin_airport_id, candidate.destination_airport_id,
            candidate.departure_time, candidate.arrival_time, 'SCHEDULED'
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
SELECT COUNT(*) AS newly_inserted_flights FROM inserted_flights;

-- Each flight receives twelve seats. Fares vary by route, travel date, cabin,
-- and seat position, so repeated daily services do not all cost the same.
WITH service_fares (flight_number, base_fare) AS (
    VALUES
        ('SK201',  849.00::numeric), ('SK202',  799.00::numeric),
        ('AF1550',  779.00::numeric), ('AF1551',  749.00::numeric),
        ('DY421',   499.00::numeric), ('DY422',   479.00::numeric),
        ('AF1450', 1099.00::numeric), ('AF1451', 1049.00::numeric)
), seat_templates (seat_number, seat_class, multiplier, adjustment) AS (
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
), target_flights AS (
    SELECT
        flight.id,
        fare.base_fare
            + ((flight.departure_time::date - DATE '2026-09-09') % 5) * 35.00
            AS economy_fare
    FROM flight
             JOIN service_fares AS fare ON fare.flight_number = flight.flight_number
    WHERE flight.departure_time::date
              BETWEEN DATE '2026-09-09' AND DATE '2026-09-24'
)
INSERT INTO flight_seat (seat_number, seat_class, price, flight_id)
SELECT
    seat.seat_number,
    seat.seat_class,
    ROUND(flight.economy_fare * seat.multiplier + seat.adjustment, 2),
    flight.id
FROM target_flights AS flight
         CROSS JOIN seat_templates AS seat
WHERE NOT EXISTS (
    SELECT 1
    FROM flight_seat AS existing
    WHERE existing.flight_id = flight.id
      AND existing.seat_number = seat.seat_number
);

SELECT 'Daily multi-airline flights (expected 128)' AS entity, COUNT(*) AS total
FROM flight
WHERE flight_number IN ('SK201','SK202','AF1550','AF1551','DY421','DY422','AF1450','AF1451')
  AND departure_time::date BETWEEN DATE '2026-09-09' AND DATE '2026-09-24'
UNION ALL
SELECT 'Daily multi-airline seats (expected 1536)', COUNT(*)
FROM flight_seat AS seat
         JOIN flight ON flight.id = seat.flight_id
WHERE flight.flight_number IN ('SK201','SK202','AF1550','AF1551','DY421','DY422','AF1450','AF1451')
  AND flight.departure_time::date BETWEEN DATE '2026-09-09' AND DATE '2026-09-24';
