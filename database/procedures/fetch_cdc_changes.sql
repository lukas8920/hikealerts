CREATE PROCEDURE FetchCDCChanges
AS
BEGIN
    SET NOCOUNT ON; -- Prevent extra result sets from interfering with SELECT statements.

    DECLARE @LastLSN binary(10);
    SELECT @LastLSN = last_lsn FROM last_processed_lsn;

    -- Common Table Expression to fetch latest changes
    WITH LatestChanges AS (
        SELECT 
            *,
            ROW_NUMBER() OVER (PARTITION BY id ORDER BY __$start_lsn DESC) AS rn
        FROM 
            cdc.dbo_us_raw_events_CT
        WHERE 
            __$operation IN (2, 4) -- Include both insert (2) and update (4)
            AND __$start_lsn > @LastLSN -- Only fetch changes after the last processed LSN
    )
    SELECT 
        id, title, create_date, park_code, description, url
    FROM 
        LatestChanges
    WHERE 
        rn = 1; -- Fetch only the latest change for each unique identifier

    -- Update the last processed LSN
    DECLARE @MaxLSN binary(10);
    SELECT @MaxLSN = MAX(__$start_lsn) 
    FROM cdc.dbo_us_raw_events_CT 
    WHERE __$operation IN (2, 4) AND __$start_lsn > @LastLSN;

    IF @MaxLSN IS NOT NULL
    BEGIN
        UPDATE last_processed_lsn SET last_lsn = @MaxLSN;
    END
END;
