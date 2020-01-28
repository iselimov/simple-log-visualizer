delete from logger.log_root;

alter table logger.log_source drop column type;

alter table logger.log_root add column uid varchar(36) not null;

alter table logger.log_root rename column start_date to first_action_date;
alter table logger.log_root rename column end_date to last_action_date;
alter table logger.log_root alter column last_action_date set not null;
alter table logger.log_root rename column update_date to creation_date;

alter table logger.log_root drop column payload_name;
alter table logger.log_root drop column description;
alter table logger.log_root drop column patient;

alter table logger.log add column invocation_order INT not null;
alter table logger.log add column depth int;
alter table logger.log add column args varchar(1024);
alter table logger.log add column patient INT;
alter table logger.log add column exception varchar(1024);

alter table logger.log rename column marker to event_type;
alter table logger.log rename column description to full_message;

alter table logger.log alter column action_name drop not null;

create sequence logger.sparql_query_id_seq increment 50;
CREATE TABLE logger.sparql_query
(
    id               bigint primary key,
    start_action             BIGINT        not null
        constraint "fk_sparql_query"
            REFERENCES logger.log (id) ON DELETE CASCADE ON UPDATE NO ACTION,
    "timestamp"   timestamp not null,
    timing             bigint  not null
);
