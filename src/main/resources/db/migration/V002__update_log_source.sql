alter table logger.log_source add column last_success_update timestamp not null;
alter table logger.log_source add column last_update_error text;
