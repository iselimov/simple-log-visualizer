alter table logger.log_root add column scoring_uuid varchar(32) not null;

alter table logger.log add column invocation_count INT not null;

create sequence logger.sparql_request_id_seq increment 50;
CREATE TABLE logger.sparql_request
(
    id               bigint primary key,
    start_action             BIGINT        not null
        constraint "fk_sparql_request_log"
            REFERENCES logger.log (id) ON DELETE CASCADE ON UPDATE NO ACTION,
    timing             int  not null
);

