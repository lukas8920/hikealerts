CREATE TABLE [hiking-sql-db].dbo.user_roles (
    id int IDENTITY(1,1) NOT NULL,
    user_id int NOT NULL,
    roles varchar(20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    CONSTRAINT PK__users_t__3213E83FE0F9F7F5 PRIMARY KEY (id),
    CONSTRAINT FK__users_tr__user__787EE5A0 FOREIGN KEY (user_id) REFERENCES [hiking-sql-db].dbo.users(id)
);