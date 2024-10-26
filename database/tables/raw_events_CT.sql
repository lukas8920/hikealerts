CREATE TABLE [hiking-sql-db].dbo.raw_events_CT (
    change_id int IDENTITY(1,1) NOT NULL,
    id int NULL,
    event_id varchar(40) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    country nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    title nvarchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    create_date datetime NULL,
    park_code nvarchar(50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    description nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    url nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    operation char(1) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    change_timestamp datetime DEFAULT getdate() NULL,
    CONSTRAINT PK__raw_even__F4EFE5968FEE0A98 PRIMARY KEY (change_id)
);