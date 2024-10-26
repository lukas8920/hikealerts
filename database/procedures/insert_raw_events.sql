CREATE PROCEDURE InsertRawEvents
    @event_id varchar(40),
	@country varchar(2),
	@title NVARCHAR(MAX),
	@park_code NVARCHAR(MAX),
	@description NVARCHAR(MAX),
    @url NVARCHAR(MAX)
AS
BEGIN
MERGE INTO dbo.raw_events AS target
    USING (SELECT @event_id as event_id, @country as country, @title as title, @park_code as park_code, @description as description, @url as url) AS source
    ON target.event_id = source.event_id AND target.country = source.country
    WHEN MATCHED AND (target.title <> source.title or target.park_code <> source.park_code or target.description <> source.description or target.url <> source.url)
        THEN
        UPDATE SET target.title = source.title, target.park_code = source.park_code, target.description = source.description, target.url = source.url
    WHEN NOT MATCHED THEN
        INSERT (event_id, country, title, create_date, park_code, description, url) VALUES (source.event_id, source.country, source.title, current_timestamp, source.park_code, source.description, source.url);
END
