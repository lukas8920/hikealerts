CREATE TABLE [prod-testapp-nz-sql].dbo.us_geodata_regions (
	id varchar(10) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	code varchar(10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	name varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	boundaries geometry NULL
);
