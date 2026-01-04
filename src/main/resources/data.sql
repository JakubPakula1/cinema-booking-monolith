INSERT INTO rooms (name) VALUES ('Sala IMAX');
INSERT INTO rooms (name) VALUES ('Sala Kameralna');

INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 1, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 2, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 3, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 4, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 5, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 6, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 7, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 8, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 9, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (1, 10, 2);

INSERT INTO seats (row_number, seat_number, room_id) VALUES (2, 1, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (2, 2, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (2, 3, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (2, 4, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (2, 5, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (2, 6, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (2, 7, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (2, 8, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (2, 9, 2);
INSERT INTO seats (row_number, seat_number, room_id) VALUES (2, 10, 2);

INSERT INTO ticket_type (name, price) VALUES ('Normal', 25.00);
INSERT INTO ticket_type (name, price) VALUES ('Reduced', 18.00);
INSERT INTO ticket_type (name, price) VALUES ('Family', 15.00);
--
-- INSERT INTO movies (title, description, genre, duration_minutes, poster_url)
-- VALUES ('Diuna: Część Druga', 'Paul Atreides łączy siły z Chani...', 'Sci-Fi', 166, 'https://link.do/obrazka.jpg');
--
-- INSERT INTO movies (title, description, genre, duration_minutes, poster_url)
-- VALUES ('Kung Fu Panda 5', 'Po wyrusza na kolejną przygodę.', 'Animacja', 95, 'https://link.do/panda.jpg');