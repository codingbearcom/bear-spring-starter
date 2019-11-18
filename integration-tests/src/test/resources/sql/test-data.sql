--
-- Copyright (c) 2019 Coding Bear s.r.o.
--
insert into test_user (id, first_name, last_name)
values (1, 'John', 'Doe');

alter SEQUENCE public.test_user_id_seq restart with 2;
