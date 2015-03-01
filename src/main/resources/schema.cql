DROP KEYSPACE IF EXISTS ctrl_pkw;
CREATE KEYSPACE ctrl_pkw WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1 };
USE ctrl_pkw;

CREATE TYPE ballot (
    voting_date timestamp,
    ballot_no int
);

CREATE TYPE ward (
    community_code text,
    ward_no int
);

CREATE TABLE protocol (
    id uuid,
    ballot frozen<ballot>,
    ward frozen<ward>,
    voters_entitled_count bigint,
    ballots_given_count bigint,
    votes_cast_count bigint,
    votes_valid_count bigint,
    votes_count_per_option list<bigint>,
    cloudinary_cloud_name varchar,
    PRIMARY KEY (id, ballot, ward)
);
