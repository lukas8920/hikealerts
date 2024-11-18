CREATE PROCEDURE InsertRawEvents
    @event_id varchar(40),
	@country varchar(2),
	@title NVARCHAR(MAX),
	@park_code NVARCHAR(MAX),
	@description NVARCHAR(MAX),
    @url NVARCHAR(MAX),
    @publisher_id int,
    @park_region_name varchar(100),
    @in_start_date_time varchar(20),
    @in_end_date_time varchar(20)
AS
BEGIN
	DECLARE @start_date_time DATETIME;
	DECLARE @end_date_time DATETIME;

	IF @in_start_date_time = 'None'
BEGIN
		SET @start_date_time = NULL
END
ELSE
BEGIN
		SET @start_date_time = @in_start_date_time
END
	IF @in_end_date_time = 'None'
BEGIN
		SET @end_date_time = NULL
END
ELSE
BEGIN
		SET @end_date_time = @in_end_date_time
END


BEGIN
MERGE INTO dbo.raw_events AS target
    USING (SELECT @event_id as event_id, @country as country, @title as title, @park_code as park_code, @description as description, @url as url, @publisher_id as publisher_id, @park_region_name as park_region_name, @start_date_time as start_date_time, @end_date_time as end_date_time) AS source
    ON target.event_id = source.event_id AND target.country = source.country
    WHEN MATCHED AND (target.title <> source.title or target.park_code <> source.park_code or target.description <> source.description or target.url <> source.url or target.publisher_id <> source.publisher_id or target.park_region_name <> source.park_region_name or target.start_date_time <> source.start_date_time or target.end_date_time <> source.end_date_time)
        THEN
        UPDATE SET target.title = CASE WHEN source.title <> 'None' THEN source.title ELSE target.title END, target.park_code = source.park_code, target.description = source.description, target.url = source.url, target.publisher_id = source.publisher_id, target.park_region_name = source.park_region_name, target.start_date_time = source.start_date_time, target.end_date_time = source.end_date_time
    WHEN NOT MATCHED THEN
        INSERT (event_id, country, title, create_date, park_code, description, url, publisher_id, park_region_name, start_date_time, end_date_time) VALUES (source.event_id, source.country, source.title, current_timestamp, source.park_code, source.description, source.url, source.publisher_id, source.park_region_name, source.start_date_time, source.end_date_time);
END
END