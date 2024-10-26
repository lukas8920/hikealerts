CREATE TABLE [hiking-sql-db].dbo.geodata_trails (
    id int IDENTITY(1,1) NOT NULL,
    trail_id varchar(40) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    trailname varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    country varchar(2) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    maplabel varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    unitcode varchar(10) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    unitname varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    regioncode varchar(15) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    maintainer varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    coordinates geometry NULL
);
