CREATE TABLE [hiking-sql-db].dbo.publisher (
    id int IDENTITY(1,1) NOT NULL,
    name varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    status varchar(10) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    copyright varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    license varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
);