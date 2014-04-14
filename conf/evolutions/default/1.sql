# Discs schema
 
# --- !Ups

CREATE SEQUENCE disc_id_seq;
CREATE TABLE disc (
    id integer NOT NULL DEFAULT nextval('disc_id_seq'),
    label varchar(255)
);
 
# --- !Downs
 
DROP TABLE disc;
DROP SEQUENCE disc_id_seq;