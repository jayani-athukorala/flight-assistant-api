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

-- PostgreSQL development data. Reference date: 2026-09-08.
-- Requires audit columns: created_by_user_id and last_modified_by_user_id.
-- Repeat-safe: existing records are preserved.
BEGIN;

-- SQL initialization runs before FlightBookingDataRunner, so create its users here.
INSERT INTO users (
    email,
    password,
    role,
    created_at,
    updated_at,
    created_by_user_id,
    last_modified_by_user_id
)
VALUES
    (
        'admin@test.com',
        '$2b$12$OuapsHkCBQOo6lBwZMN25uFZ/Q7AgY79q2K1M0F2JHltLZZSvA1U2',
        'ADMIN',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL,
        NULL
    ),
    (
        'john@test.com',
        '$2b$12$hKUNTDmEIW2TNyugntgZCuD2e6qn6DNTwtqA17zooD.QJRtty1BAS',
        'USER',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL,
        NULL
    ),
    (
        'jane@test.com',
        '$2b$12$hKUNTDmEIW2TNyugntgZCuD2e6qn6DNTwtqA17zooD.QJRtty1BAS',
        'USER',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        NULL,
        NULL
    )
ON CONFLICT (email) DO NOTHING;

-- After ADMIN has an ID, mark ADMIN as the creator and modifier of all
-- three development accounts. ADMIN therefore audits its own seed record.
UPDATE users AS target
SET created_by_user_id =
        COALESCE(target.created_by_user_id, admin.id),
    last_modified_by_user_id =
        COALESCE(target.last_modified_by_user_id, admin.id),
    updated_at =
        CASE
            WHEN target.created_by_user_id IS NULL
                OR target.last_modified_by_user_id IS NULL
                THEN CURRENT_TIMESTAMP
            ELSE target.updated_at
            END
FROM users AS admin
WHERE admin.email = 'admin@test.com'
  AND target.email IN (
                       'admin@test.com',
                       'john@test.com',
                       'jane@test.com'
    );

INSERT INTO airports (code,name,city,country) VALUES
                                                  ('ARN','Stockholm Arlanda Airport','Stockholm','Sweden'),
                                                  ('GOT','Goteborg Landvetter Airport','Gothenburg','Sweden'),
                                                  ('CDG','Charles de Gaulle Airport','Paris','France'),
                                                  ('LHR','Heathrow Airport','London','United Kingdom'),
                                                  ('AMS','Amsterdam Airport Schiphol','Amsterdam','Netherlands'),
                                                  ('FRA','Frankfurt Airport','Frankfurt','Germany'),
                                                  ('BER','Berlin Brandenburg Airport','Berlin','Germany'),
                                                  ('MAD','Adolfo Suarez Madrid-Barajas Airport','Madrid','Spain'),
                                                  ('BCN','Barcelona-El Prat Airport','Barcelona','Spain'),
                                                  ('FCO','Leonardo da Vinci-Fiumicino Airport','Rome','Italy'),
                                                  ('CPH','Copenhagen Airport','Copenhagen','Denmark'),
                                                  ('VIE','Vienna International Airport','Vienna','Austria'),
                                                  ('OSL','Oslo Airport Gardermoen','Oslo','Norway'),
                                                  ('HEL','Helsinki Airport','Helsinki','Finland'),
                                                  ('DUB','Dublin Airport','Dublin','Ireland')
ON CONFLICT (code) DO NOTHING;

-- Four outbound and four return services daily from 1 through 24 September.
WITH services(number,airline,origin,destination,departure,duration) AS (
    VALUES
        ('SK201','SAS','GOT','CDG',TIME '08:15',135),
        ('SK202','SAS','CDG','GOT',TIME '16:30',130),
        ('AF1550','Air France','GOT','CDG',TIME '13:45',140),
        ('AF1551','Air France','CDG','GOT',TIME '19:15',135),
        ('DY421','Norwegian','GOT','ARN',TIME '06:45',60),
        ('DY422','Norwegian','ARN','GOT',TIME '18:10',60),
        ('AF1450','Air France','ARN','CDG',TIME '09:20',160),
        ('AF1451','Air France','CDG','ARN',TIME '15:40',155)
), candidates AS (
    SELECT s.number,s.airline,o.id origin_id,x.id destination_id,
           d.day::date+s.departure departure_time,
           d.day::date+s.departure+s.duration*INTERVAL '1 minute' arrival_time,
           CASE
               WHEN d.day::date<=DATE '2026-09-08' THEN 'COMPLETED'
               WHEN (s.number,d.day::date) IN (
                                               ('SK202',DATE '2026-09-11'),
                                               ('AF1450',DATE '2026-09-14'),
                                               ('DY422',DATE '2026-09-18')) THEN 'CANCELLED'
               ELSE 'SCHEDULED'
               END status,a.id admin_id
    FROM generate_series(DATE '2026-09-01',DATE '2026-09-24',INTERVAL '1 day') d(day)
             CROSS JOIN services s
             JOIN airports o ON o.code=s.origin
             JOIN airports x ON x.code=s.destination
             CROSS JOIN (SELECT id FROM users WHERE email='admin@test.com') a
)
INSERT INTO flight
(flight_number,airline,origin_airport_id,destination_airport_id,
 departure_time,arrival_time,status,created_at,updated_at,
 created_by_user_id,last_modified_by_user_id)
SELECT number,airline,origin_id,destination_id,departure_time,arrival_time,status,
       CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,admin_id,admin_id
FROM candidates c
WHERE NOT EXISTS (
    SELECT 1 FROM flight f
    WHERE f.flight_number=c.number AND f.origin_airport_id=c.origin_id
      AND f.destination_airport_id=c.destination_id
      AND f.departure_time=c.departure_time
);

-- Enforce the requested examples again when the script is rerun.
UPDATE flight f SET
                    status=CASE
                               WHEN departure_time::date<=DATE '2026-09-08' THEN 'COMPLETED'
                               WHEN (flight_number,departure_time::date) IN (
                                                                             ('SK202',DATE '2026-09-11'),
                                                                             ('AF1450',DATE '2026-09-14'),
                                                                             ('DY422',DATE '2026-09-18')) THEN 'CANCELLED'
                               ELSE status END,
                    updated_at=CURRENT_TIMESTAMP,
                    last_modified_by_user_id=(SELECT id FROM users WHERE email='admin@test.com')
WHERE departure_time::date BETWEEN DATE '2026-09-01' AND DATE '2026-09-24'
  AND flight_number IN ('SK201','SK202','AF1550','AF1551','DY421','DY422','AF1450','AF1451');

-- Dedicated future flight that will become FULL after every seat is booked.
INSERT INTO flight
(flight_number,airline,origin_airport_id,destination_airport_id,
 departure_time,arrival_time,status,created_at,updated_at,
 created_by_user_id,last_modified_by_user_id)
SELECT 'SR900','SkyRoute',o.id,d.id,TIMESTAMP '2026-09-10 12:00',
       TIMESTAMP '2026-09-10 14:00','SCHEDULED',CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP,a.id,a.id
FROM airports o CROSS JOIN airports d
                CROSS JOIN (SELECT id FROM users WHERE email='admin@test.com') a
WHERE o.code='GOT' AND d.code='LHR'
  AND NOT EXISTS (
    SELECT 1 FROM flight
    WHERE flight_number='SR900' AND departure_time=TIMESTAMP '2026-09-10 12:00'
);

-- Twelve seats per flight. The lowest seat price can be used as “starting from”.
WITH fares(number,base) AS (
    VALUES ('SK201',849.00::numeric),('SK202',799.00::numeric),
           ('AF1550',779.00::numeric),('AF1551',749.00::numeric),
           ('DY421',499.00::numeric),('DY422',479.00::numeric),
           ('AF1450',1099.00::numeric),('AF1451',1049.00::numeric),
           ('SR900',690.00::numeric)
), seats(number,class,multiplier,extra) AS (
    VALUES
        ('1A','FIRST_CLASS',3.20::numeric,500.00::numeric),
        ('2A','BUSINESS',2.10::numeric,250.00::numeric),
        ('2B','BUSINESS',2.10::numeric,250.00::numeric),
        ('5A','PREMIUM_ECONOMY',1.45::numeric,120.00::numeric),
        ('5B','PREMIUM_ECONOMY',1.45::numeric,120.00::numeric),
        ('10A','ECONOMY',1.00::numeric,80.00::numeric),
        ('10B','ECONOMY',1.00::numeric,80.00::numeric),
        ('11A','ECONOMY',1.00::numeric,50.00::numeric),
        ('11B','ECONOMY',1.00::numeric,50.00::numeric),
        ('12A','ECONOMY',1.00::numeric,20.00::numeric),
        ('12B','ECONOMY',1.00::numeric,20.00::numeric),
        ('12C','ECONOMY',1.00::numeric,0.00::numeric)
), targets AS (
    SELECT f.id,fa.base+((f.departure_time::date-DATE '2026-09-01')%5)*35.00 fare
    FROM flight f JOIN fares fa ON fa.number=f.flight_number
    WHERE f.departure_time::date BETWEEN DATE '2026-09-01' AND DATE '2026-09-24'
)
INSERT INTO flight_seat (seat_number,seat_class,price,flight_id)
SELECT s.number,s.class,ROUND(t.fare*s.multiplier+s.extra,2),t.id
FROM targets t CROSS JOIN seats s
WHERE NOT EXISTS (
    SELECT 1 FROM flight_seat fs WHERE fs.flight_id=t.id AND fs.seat_number=s.number
);

-- Passengers are travellers, not booking owners. ADMIN is their audit creator.
WITH data(passport,first_name,last_name,email) AS (
    VALUES
        ('SR900-P001','Anna','Andersson','anna.sr900@test.com'),
        ('SR900-P002','Erik','Berg','erik.sr900@test.com'),
        ('SR900-P003','Sara','Carlsson','sara.sr900@test.com'),
        ('SR900-P004','Lars','Dahl','lars.sr900@test.com'),
        ('SR900-P005','Maja','Ek','maja.sr900@test.com'),
        ('SR900-P006','Oskar','Fors','oskar.sr900@test.com'),
        ('SR900-P007','Elin','Gustafsson','elin.sr900@test.com'),
        ('SR900-P008','Nils','Holm','nils.sr900@test.com'),
        ('SR900-P009','Ida','Isaksson','ida.sr900@test.com'),
        ('SR900-P010','Viktor','Jansson','viktor.sr900@test.com'),
        ('SR900-P011','Emma','Karlsson','emma.sr900@test.com'),
        ('SR900-P012','Axel','Lind','axel.sr900@test.com')
)
INSERT INTO passenger
(first_name,last_name,email,passport_number,created_at,updated_at,
 created_by_user_id,last_modified_by_user_id)
SELECT d.first_name,d.last_name,d.email,d.passport,
       CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,a.id,a.id
FROM data d CROSS JOIN (SELECT id FROM users WHERE email='admin@test.com') a
WHERE NOT EXISTS (
    SELECT 1 FROM passenger p WHERE p.passport_number=d.passport
);

-- John and Jane own one booking each; ADMIN created both bookings.
WITH ff AS (
    SELECT id FROM flight
    WHERE flight_number='SR900' AND departure_time=TIMESTAMP '2026-09-10 12:00'
), ranked AS (
    SELECT fs.price,ROW_NUMBER() OVER (ORDER BY fs.id) rn
    FROM flight_seat fs JOIN ff ON ff.id=fs.flight_id
), data(reference,owner,first_seat,last_seat) AS (
    VALUES ('FB-SR900-JOHN','john@test.com',1,6),
           ('FB-SR900-JANE','jane@test.com',7,12)
)
INSERT INTO booking
(booking_reference,booking_date,status,trip_type,total_price,user_id,
 outbound_flight_id,return_flight_id,created_at,updated_at,
 created_by_user_id,last_modified_by_user_id)
SELECT d.reference,CURRENT_TIMESTAMP,'CONFIRMED','ONE_WAY',SUM(r.price),u.id,ff.id,NULL,
       CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,a.id,a.id
FROM data d JOIN users u ON u.email=d.owner CROSS JOIN ff
            CROSS JOIN (SELECT id FROM users WHERE email='admin@test.com') a
            JOIN ranked r ON r.rn BETWEEN d.first_seat AND d.last_seat
WHERE NOT EXISTS (
    SELECT 1 FROM booking b WHERE b.booking_reference=d.reference
)
GROUP BY d.reference,u.id,ff.id,a.id;

WITH people AS (
    SELECT p.id,ROW_NUMBER() OVER (ORDER BY p.passport_number) rn
    FROM passenger p WHERE p.passport_number LIKE 'SR900-P%'
), allocation(reference,first_number,last_number) AS (
    VALUES ('FB-SR900-JOHN',1,6),('FB-SR900-JANE',7,12)
)
INSERT INTO booking_passengers (booking_id,passenger_id)
SELECT b.id,p.id FROM allocation a
                          JOIN booking b ON b.booking_reference=a.reference
                          JOIN people p ON p.rn BETWEEN a.first_number AND a.last_number
WHERE NOT EXISTS (
    SELECT 1 FROM booking_passengers bp
    WHERE bp.booking_id=b.id AND bp.passenger_id=p.id
);

WITH ff AS (
    SELECT id FROM flight
    WHERE flight_number='SR900' AND departure_time=TIMESTAMP '2026-09-10 12:00'
), seats AS (
    SELECT fs.id,ROW_NUMBER() OVER (ORDER BY fs.id) rn
    FROM flight_seat fs JOIN ff ON ff.id=fs.flight_id
), people AS (
    SELECT p.id,ROW_NUMBER() OVER (ORDER BY p.passport_number) rn
    FROM passenger p WHERE p.passport_number LIKE 'SR900-P%'
), allocation(reference,first_number,last_number) AS (
    VALUES ('FB-SR900-JOHN',1,6),('FB-SR900-JANE',7,12)
)
INSERT INTO booking_seat
(booking_id,seat_id,passenger_id,created_at,updated_at,
 created_by_user_id,last_modified_by_user_id)
SELECT b.id,s.id,p.id,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,audit.id,audit.id
FROM allocation a
         JOIN booking b ON b.booking_reference=a.reference
         JOIN seats s ON s.rn BETWEEN a.first_number AND a.last_number
         JOIN people p ON p.rn=s.rn
         CROSS JOIN (SELECT id FROM users WHERE email='admin@test.com') audit
WHERE NOT EXISTS (
    SELECT 1 FROM booking_seat bs WHERE bs.seat_id=s.id
);

-- Mark FULL only when all seats belong to confirmed bookings.
UPDATE flight f SET status='FULL',updated_at=CURRENT_TIMESTAMP,
                    last_modified_by_user_id=(SELECT id FROM users WHERE email='admin@test.com')
WHERE f.flight_number='SR900'
  AND f.departure_time=TIMESTAMP '2026-09-10 12:00'
  AND (SELECT COUNT(*) FROM flight_seat fs WHERE fs.flight_id=f.id)>0
  AND (SELECT COUNT(*) FROM flight_seat fs WHERE fs.flight_id=f.id)=
      (SELECT COUNT(DISTINCT bs.seat_id)
       FROM booking_seat bs
                JOIN booking b ON b.id=bs.booking_id
                JOIN flight_seat fs ON fs.id=bs.seat_id
       WHERE fs.flight_id=f.id AND b.status='CONFIRMED');

COMMIT;

-- Verification: clean database => 193 flights and 2,316 seats.
SELECT status,COUNT(*) flights FROM flight
WHERE departure_time::date BETWEEN DATE '2026-09-01' AND DATE '2026-09-24'
GROUP BY status ORDER BY status;

SELECT f.flight_number,f.status,u.email created_by,
       COUNT(DISTINCT fs.id) total_seats,
       COUNT(DISTINCT CASE WHEN b.status='CONFIRMED' THEN bs.seat_id END) booked_seats
FROM flight f
         LEFT JOIN users u ON u.id=f.created_by_user_id
         LEFT JOIN flight_seat fs ON fs.flight_id=f.id
         LEFT JOIN booking_seat bs ON bs.seat_id=fs.id
         LEFT JOIN booking b ON b.id=bs.booking_id
WHERE f.flight_number='SR900' AND f.departure_time=TIMESTAMP '2026-09-10 12:00'
GROUP BY f.flight_number,f.status,u.email;
