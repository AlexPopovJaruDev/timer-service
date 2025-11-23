--liquibase formatted sql

--changeset jarudev:create-time-records-table
CREATE TABLE IF NOT EXISTS time_records
(
    time TIMESTAMP PRIMARY KEY
);