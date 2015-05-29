drop table if exists ratings;
create table ratings
(
	userid		integer not null,
	movieid		integer not null,
	rating		integer,
	timestamp	integer,
	constraint ratings_pk primary key (userid, movieid)
);

drop table if exists movies;
create table movies
(
	movieid		integer not null,
	title		varchar,
	genres		varchar,
	constraint movies_pk primary key (movieid)
);

drop table if exists recommendations;
create table recommendations
(
	userid		integer not null,
	rec1		integer,
	rec2		integer,
	rec3		integer,
	rec4		integer,
	rec5		integer,
	constraint rec_pk primary key(userid)
);
