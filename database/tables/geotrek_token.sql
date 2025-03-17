CREATE TABLE [hiking-sql-db].dbo.geotrek_token (
    id int IDENTITY(1,1) NOT NULL,
    user_id int NOT NULL,
    user_name varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    password varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
);