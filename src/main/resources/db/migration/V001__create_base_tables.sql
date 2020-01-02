create schema logger;

create sequence logger.log_source_id_seq increment 50;
CREATE TABLE logger.log_source
(
    id               bigint primary key,
    name             varchar(100) not null,
    type             varchar(50)  not null,
    graylog_uid      varchar(50),
    graylog_timezone VARCHAR(50)
);

create sequence logger.log_root_id_seq increment 50;
CREATE TABLE logger.log_root
(
    id           BIGINT PRIMARY KEY,
    source       bigint        not null
        constraint "fk_log_root_log_source"
            REFERENCES logger.log_source (id) ON DELETE CASCADE ON UPDATE NO ACTION,
    payload_name varchar(256)  not null,
    description  varchar(2048) not null,
    patient      bigint        not null,
    start_date   timestamp     not null,
    end_date     timestamp,
    update_date  timestamp     not null
);

create sequence logger.log_id_seq increment 50;
CREATE TABLE logger.log
(
    id          BIGINT PRIMARY KEY,
    root        BIGINT        not null
        constraint "fk_log_log_root"
            REFERENCES logger.log_root (id) ON DELETE CASCADE ON UPDATE NO ACTION,
    action_name varchar(256)  not null,
    description varchar(2048) not null,
    "timestamp" timestamp     not null
);
