-- [hiking-sql-db].dbo.job_schedule definition

-- Drop table

-- DROP TABLE [hiking-sql-db].dbo.job_schedule;

CREATE TABLE [hiking-sql-db].dbo.job_schedule (
    job_name varchar(255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    kettle_job_path varchar(500) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
    run_interval_minutes int NOT NULL,
    is_active bit NOT NULL,
    CONSTRAINT PK__job_sche__579081835403AAEF PRIMARY KEY (job_name)
    );