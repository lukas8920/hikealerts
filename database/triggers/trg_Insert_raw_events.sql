CREATE TRIGGER trg_Insert_raw_events
ON dbo.raw_events
AFTER INSERT
AS
BEGIN
    INSERT INTO dbo.raw_events_CT (id, event_id, country, title, create_date, park_code, description, url, operation)
    SELECT id, event_id, country, title, create_date, park_code, description, url, 'I'
    FROM inserted;
END;