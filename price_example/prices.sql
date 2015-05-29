drop table if exists PRICES;
drop table if exists prices;

create table PRICES (
 SYMBOL varchar(10),
 DATE   varchar(10),
 TIME varchar(10),
 OPEN varchar(10),
 HIGH varchar(10),
 LOW    varchar(10),
 CLOSE     varchar(10),
 VOLUME varchar(30),
 CONSTRAINT pk PRIMARY KEY (VOLUME)
);
