INSERT INTO voting(id, voting_date, description) values (1, DATE '2010-06-20', 'Wybory na Prezydenta Rzeczypospolitej Polskiej');

INSERT INTO ballot(id, voting_id, no, question) values (1, 1, 1, 'Lista kandydatów na Prezydenta Rzeczypospolitej Polskiej');

INSERT INTO ballot_option(id, ballot_id, no, description) values(1, 1, 1, 'JUREK Marek');
INSERT INTO ballot_option(id, ballot_id, no, description) values(2, 1, 2, 'KACZYŃSKI Jarosław Aleksander');
INSERT INTO ballot_option(id, ballot_id, no, description) values(3, 1, 3, 'KOMOROWSKI Bronisław Maria');
INSERT INTO ballot_option(id, ballot_id, no, description) values(4, 1, 4, 'KORWIN-MIKKE Janusz Ryszard');
INSERT INTO ballot_option(id, ballot_id, no, description) values(5, 1, 5, 'LEPPER Andrzej Zbigniew');
INSERT INTO ballot_option(id, ballot_id, no, description) values(6, 1, 6, 'MORAWIECKI Kornel Andrzej');
INSERT INTO ballot_option(id, ballot_id, no, description) values(7, 1, 7, 'NAPIERALSKI Grzegorz Bernard');
INSERT INTO ballot_option(id, ballot_id, no, description) values(8, 1, 8, 'OLECHOWSKI Andrzej Marian');
INSERT INTO ballot_option(id, ballot_id, no, description) values(9, 1, 9, 'PAWLAK Waldemar');
INSERT INTO ballot_option(id, ballot_id, no, description) values(10, 1, 10, 'ZIĘTEK Bogusław Zbigniew');
