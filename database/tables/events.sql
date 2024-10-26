CREATE TABLE [hiking-sql-db].dbo.events (
    id int IDENTITY(1,1) NOT NULL,
    event_id varchar(40) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    region varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    country varchar(2) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    create_date_time datetime NULL,
    from_date_time datetime NULL,
    to_date_time datetime NULL,
    trail_id int NOT NULL,
    mid_longitude_coordinate float NULL,
    mid_latitude_coordinate float NULL,
    title varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    description nvarchar(MAX) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    publisher_id int NULL,
    url varchar(200) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    CONSTRAINT events_pk PRIMARY KEY (id)
);