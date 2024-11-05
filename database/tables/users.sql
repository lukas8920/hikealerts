CREATE TABLE [hiking-sql-db].dbo.users (
    id int IDENTITY(1,1) NOT NULL,
    mail varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    password varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    enabled bit NULL,
    publisher_id int NULL,
    api_key varchar(100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    CONSTRAINT users_pk PRIMARY KEY (id)
);