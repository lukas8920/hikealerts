CREATE TABLE [hiking-sql-db].dbo.raw_events (
	id int IDENTITY(1,1) NOT NULL,
	event_id varchar(40) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	country varchar(2) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	title varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	create_date datetime NULL,
	park_code varchar(10) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	description nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	url varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	CONSTRAINT raw_events_pk PRIMARY KEY (id)
);
