CREATE TABLE [prod-testapp-nz-sql].dbo.us_geodata_trails (
	id int IDENTITY(1,1) NOT NULL,
	trailname varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	maplabel varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	unitcode varchar(10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	unitname varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	regioncode varchar(15) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	maintainer varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	coordinates geometry NULL
);
