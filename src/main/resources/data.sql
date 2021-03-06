INSERT INTO voting(id, voting_date, description) values (3, DATE '2015-05-24', 'Wybory na Prezydenta Rzeczypospolitej Polskiej (2. głosowanie)');
INSERT INTO voting(id, voting_date, description) values (2, DATE '2015-05-10', 'Wybory na Prezydenta Rzeczypospolitej Polskiej (1. głosowanie)');
INSERT INTO voting(id, voting_date, description) values (1, DATE '2010-06-20', 'Wybory na Prezydenta Rzeczypospolitej Polskiej (test na podstawie danych z 2010r)');

INSERT INTO ballot(id, voting_id, no, question) values (11, 1, 1, 'Lista kandydatów na Prezydenta Rzeczypospolitej Polskiej');

INSERT INTO ballot_option(id, ballot_id, no, description) values(111, 11, 1, 'JUREK Marek');
INSERT INTO ballot_option(id, ballot_id, no, description) values(112, 11, 2, 'KACZYŃSKI Jarosław Aleksander');
INSERT INTO ballot_option(id, ballot_id, no, description) values(113, 11, 3, 'KOMOROWSKI Bronisław Maria');
INSERT INTO ballot_option(id, ballot_id, no, description) values(114, 11, 4, 'KORWIN-MIKKE Janusz Ryszard');
INSERT INTO ballot_option(id, ballot_id, no, description) values(115, 11, 5, 'LEPPER Andrzej Zbigniew');
INSERT INTO ballot_option(id, ballot_id, no, description) values(116, 11, 6, 'MORAWIECKI Kornel Andrzej');
INSERT INTO ballot_option(id, ballot_id, no, description) values(117, 11, 7, 'NAPIERALSKI Grzegorz Bernard');
INSERT INTO ballot_option(id, ballot_id, no, description) values(118, 11, 8, 'OLECHOWSKI Andrzej Marian');
INSERT INTO ballot_option(id, ballot_id, no, description) values(119, 11, 9, 'PAWLAK Waldemar');
INSERT INTO ballot_option(id, ballot_id, no, description) values(110, 11, 10, 'ZIĘTEK Bogusław Zbigniew');

INSERT INTO ballot(id, voting_id, no, question) values (21, 2, 1, 'Lista kandydatów na Prezydenta Rzeczypospolitej Polskiej');

INSERT INTO ballot_option(id, ballot_id, no, description) values(211, 21, 1, 'BRAUN Grzegorz Michał');
INSERT INTO ballot_option(id, ballot_id, no, description) values(212, 21, 2, 'DUDA Andrzej Sebastian');
INSERT INTO ballot_option(id, ballot_id, no, description) values(213, 21, 3, 'JARUBAS Adam Sebastian');
INSERT INTO ballot_option(id, ballot_id, no, description) values(214, 21, 4, 'KOMOROWSKI Bronisław Maria');
INSERT INTO ballot_option(id, ballot_id, no, description) values(215, 21, 5, 'KORWIN-MIKKE Janusz Ryszard');
INSERT INTO ballot_option(id, ballot_id, no, description) values(216, 21, 6, 'KOWALSKI Marian Janusz');
INSERT INTO ballot_option(id, ballot_id, no, description) values(217, 21, 7, 'KUKIZ Paweł Piotr');
INSERT INTO ballot_option(id, ballot_id, no, description) values(218, 21, 8, 'OGÓREK Magdalena Agnieszka');
INSERT INTO ballot_option(id, ballot_id, no, description) values(219, 21, 9, 'PALIKOT Janusz Marian');
INSERT INTO ballot_option(id, ballot_id, no, description) values(220, 21, 10, 'TANAJNO Paweł Jan');
INSERT INTO ballot_option(id, ballot_id, no, description) values(221, 21, 11, 'WILK Jacek');

INSERT INTO ballot(id, voting_id, no, question) values (31, 3, 1, 'Lista kandydatów na Prezydenta Rzeczypospolitej Polskiej');
INSERT INTO ballot_option(id, ballot_id, no, description) values(311, 31, 1, 'DUDA Andrzej Sebastian');
INSERT INTO ballot_option(id, ballot_id, no, description) values(312, 31, 2, 'KOMOROWSKI Bronisław Maria');

