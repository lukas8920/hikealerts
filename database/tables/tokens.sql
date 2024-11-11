CREATE TABLE [hiking-sql-db].dbo.tokens (
    id int IDENTITY(1,1) NOT NULL,
    token varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    user_id int not null,
    expiry_date date not null,
    CONSTRAINT tokens_pk PRIMARY KEY (id)
);