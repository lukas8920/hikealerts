CREATE PROCEDURE InsertRawEvents
	@id varchar(40),
	@title NVARCHAR(MAX),
	@park_code NVARCHAR(MAX),
	@description NVARCHAR(MAX),
    @url NVARCHAR(MAX)
AS
BEGIN
	MERGE INTO dbo.us_raw_events AS target
	USING (SELECT @id as id, @title as title, @park_code as park_code, @description as description, @url as url) AS source
	ON target.id = source.id 
	WHEN MATCHED AND (target.title <> source.title or target.park_code <> source.park_code or target.description <> source.description or target.url <> source.url)
	THEN
    	UPDATE SET target.title = source.title, target.park_code = source.park_code, target.description = source.description, target.url = source.url
	WHEN NOT MATCHED THEN
    	INSERT (id, title, create_date, park_code, description, url) VALUES (source.id, source.title, current_timestamp, source.park_code, source.description, source.url);
END
