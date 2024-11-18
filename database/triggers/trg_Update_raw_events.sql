CREATE TRIGGER trg_Update_raw_events
    ON dbo.raw_events
    AFTER UPDATE
              AS
BEGIN
INSERT INTO dbo.raw_events_CT (id, event_id, country, title, create_date, park_code, description, url, operation)
SELECT id, event_id, country, title, create_date, park_code, description, url, 'U'
FROM inserted
WHERE NOT (
    UPDATE(title) AND
    country IN ('IE')
    );
END;