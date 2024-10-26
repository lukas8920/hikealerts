CREATE PROCEDURE FetchRawEventChanges
    AS
BEGIN
    SET NOCOUNT ON; -- Prevent extra result sets from interfering with SELECT statements.

    -- Get the last processed timestamp
    DECLARE @LastTimestamp DATETIME;
SELECT @LastTimestamp = last_processed_time FROM dbo.last_processed_time WHERE id = 1;

-- Common Table Expression to fetch latest changes since the last processed time
WITH LatestChanges AS (
    SELECT
        *,
        ROW_NUMBER() OVER (PARTITION BY id ORDER BY change_timestamp DESC) AS rn
    FROM
        dbo.raw_events_CT
    WHERE
            operation IN ('I', 'U') -- Include both insert ('I') and update ('U')
      AND change_timestamp > @LastTimestamp -- Only fetch changes after the last processed timestamp
)
SELECT
    l.event_id, l.country, l.title, l.create_date, l.park_code, l.description, l.url
FROM
    LatestChanges l
        INNER JOIN raw_events e ON l.id = e.id
WHERE
        l.rn = 1; -- Fetch only the latest change for each unique identifier

-- Update the last processed timestamp
DECLARE @MaxTimestamp DATETIME;
SELECT @MaxTimestamp = MAX(change_timestamp)
FROM dbo.raw_events_CT
WHERE operation IN ('I', 'U') AND change_timestamp > @LastTimestamp;

-- Update last processed timestamp if there are new changes
IF @MaxTimestamp IS NOT NULL
BEGIN
UPDATE dbo.last_processed_time SET last_processed_time = @MaxTimestamp WHERE id = 1;
END
END;
