CREATE PROCEDURE UpdateRawEventTitle
    @event_id VARCHAR(20),
	@country VARCHAR(2),
	@title varchar(MAX)
AS
BEGIN
MERGE INTO dbo.raw_events AS target
    USING (SELECT @event_id as event_id, @country as country, @title as title) AS source
    ON target.event_id = source.event_id AND target.COUNTRY = source.country
    WHEN MATCHED THEN
        UPDATE SET target.title = source.title;
END