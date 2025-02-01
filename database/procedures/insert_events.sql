CREATE PROCEDURE InsertEvents
    @event_id varchar(MAX),
	@region varchar(MAX),
	@country varchar(2),
	@create_date_time datetime,
	@from_date_time datetime,
	@to_date_time datetime,
	@mid_longitude_coordinate float,
	@mid_latitude_coordinate float,
	@title varchar(MAX),
	@description varchar(MAX),
    -- trail ids
	@ids NVARCHAR(MAX),
	@publisher_id int,
	@url varchar(MAX),
	@override_data bit
AS
BEGIN
	DECLARE @OutputTable TABLE (
        id INT,
        event_id VARCHAR(MAX),
        region VARCHAR(MAX),
        country VARCHAR(2),
        create_date_time DATETIME,
        from_date_time DATETIME,
        to_date_time DATETIME,
        mid_longitude_coordinate FLOAT,
        mid_latitude_coordinate FLOAT,
        title VARCHAR(MAX),
        description VARCHAR(MAX),
        publisher_id INT,
        url VARCHAR(MAX)
    );

    MERGE INTO dbo.events AS target
    USING (SELECT @event_id as event_id, @region as region, @country as country, @create_date_time as create_date_time, @from_date_time as from_date_time,
                  @to_date_time as to_date_time, @mid_longitude_coordinate as mid_longitude_coordinate, @mid_latitude_coordinate as mid_latitude_coordinate, @title as title,
                  @description as description, @publisher_id as publisher_id, @url as url, @override_data as override_data) as source
    ON target.country = source.country and target.mid_longitude_coordinate = source.mid_longitude_coordinate and target.mid_latitude_coordinate = source.mid_latitude_coordinate and source.override_data = 1
    WHEN MATCHED THEN
        UPDATE SET target.event_id = source.event_id, target.region = source.region, target.country = source.country, target.create_date_time = source.create_date_time,
            target.from_date_time = source.from_date_time, target.to_date_time = source.to_date_time, target.mid_longitude_coordinate = source.mid_longitude_coordinate,
            target.mid_latitude_coordinate = source.mid_latitude_coordinate, target.title = source.title, target.description = source.description, target.publisher_id = source.publisher_id,
            target.url = source.url
    WHEN NOT MATCHED THEN
        INSERT (event_id, region, country, create_date_time, from_date_time, to_date_time, mid_longitude_coordinate, mid_latitude_coordinate, title, description, publisher_id, url)
            VALUES (source.event_id, source.region, source.country, source.create_date_time, source.from_date_time, source.to_date_time, source.mid_longitude_coordinate, source.mid_latitude_coordinate,
                    source.title, source.description, source.publisher_id, source.url)
    OUTPUT inserted.* INTO @OutputTable;

    SET NOCOUNT ON;

    DECLARE @TempTable TABLE (id VARCHAR(MAX));

    INSERT INTO @TempTable (id)
    SELECT value FROM STRING_SPLIT(@ids, ',');

    IF (SELECT count(id) FROM @TempTable t1 WHERE NOT EXISTS (SELECT 1 FROM events_trail_ids t2 WHERE t2.trail_ids = t1.id AND t2.event_id = (SELECT max(id) FROM @OutputTable))) > 0
      OR (SELECT count(id) FROM @TempTable) <> (SELECT count(e.trail_ids) FROM events_trail_ids e WHERE e.event_id = (SELECT max(id) FROM @OutputTable))
    BEGIN
        DELETE FROM events_trail_ids WHERE event_id = (SELECT max(id) FROM @OutputTable);
        INSERT INTO events_trail_ids (event_id, trail_ids)
        SELECT (SELECT max(id) FROM @OutputTable), id FROM @TempTable;
    END

    SELECT * FROM @OutputTable;
END
