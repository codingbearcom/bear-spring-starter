--
--  Copyright (c) 2019 Coding Bear s.r.o.
--
create table test_user
(
    id         serial not null primary key,
    first_name varchar(100),
    last_name  varchar(100)
);
