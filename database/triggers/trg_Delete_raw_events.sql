CREATE TRIGGER trg_Delete_raw_events
ON dbo.raw_events
AFTER DELETE
AS
BEGIN
    INSERT INTO dbo.raw_events_CT (id, event_id, country, title, create_date, park_code, description, url, operation)
    SELECT id, event_id, country, title, create_date, park_code, description, url, 'D'
    FROM deleted;
END;