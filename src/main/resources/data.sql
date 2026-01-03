INSERT INTO rooms (name) VALUES ('Sala IMAX');
INSERT INTO rooms (name) VALUES ('Sala Kameralna');

INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 1, 1);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 5, 1);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 5, 1);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 5, 1);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 6, 1);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 7, 1);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 8, 1);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 9, 1);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 10, 1);

INSERT INTO seats (row_number, seat_number, room_id) VALUES (5, 1, 1);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (5, 2, 1);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (5, 3, 1);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (5, 4, 1);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (5, 5, 1);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (5, 6, 1);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (5, 7, 1);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (5, 8, 1);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (5, 9, 1);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (5, 10, 1);

INSERT INTO ticket_type (name, price) VALUES ('Normal', 25.00);
INSERT INTO ticket_type (name, price) VALUES ('Reduced', 18.00);
INSERT INTO ticket_type (name, price) VALUES ('Family', 15.00);
--
-- INSERT INTO movies (title, description, genre, duration_minutes, poster_url)
-- VALUES ('Diuna: Część Druga', 'Paul Atreides łączy siły z Chani...', 'Sci-Fi', 166, 'https://link.do/obrazka.jpg');
--
-- INSERT INTO movies (title, description, genre, duration_minutes, poster_url)
-- VALUES ('Kung Fu Panda 5', 'Po wyrusza na kolejną przygodę.', 'Animacja', 95, 'https://link.do/panda.jpg');