create table post (
	id serial primary key not null unique,
	name varchar(200),
	text varchar(5000),
	link varchar(500) not null unique,
	created timestamp,
	author varchar(100),
	answers_count int,
	views_count int,
	last_message timestamp
);