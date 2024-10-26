CREATE TABLE [hiking-sql-db].dbo.geodata_regions (
    id int IDENTITY(1,1) NOT NULL,
    region_id varchar(10) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    country varchar(2) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    code varchar(10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    name varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    boundaries geometry NULL,
    CONSTRAINT geodata_regions_pk PRIMARY KEY (id)
);
