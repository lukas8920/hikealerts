CREATE TABLE [hiking-sql-db].dbo.events_trail_ids (
    id int IDENTITY(1,1) NOT NULL,
    event_id int NOT NULL,
    trail_ids varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    CONSTRAINT PK__events_t__3213E83FE0F9F7F5 PRIMARY KEY (id),
    CONSTRAINT FK__events_tr__event__787EE5A0 FOREIGN KEY (event_id) REFERENCES [hiking-sql-db].dbo.events(id)
);