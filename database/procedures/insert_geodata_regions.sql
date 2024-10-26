CREATE PROCEDURE InsertGeodataRegions
    @region_id VARCHAR(10),
	@country varchar(2),
	@code VARCHAR(10),
	@name NVARCHAR(MAX),
    @polygon NVARCHAR(MAX)
AS
BEGIN
MERGE INTO dbo.geodata_regions AS target
    USING (SELECT @region_id as region_id, @country as country, @code as code, @name as name, geometry::STGeomFromText(@polygon, 4326) AS boundaries) AS source
    ON target.region_id = source.region_id AND target.country = source.country
    WHEN MATCHED THEN
        UPDATE SET target.country = source.country, target.code = source.code, target.name = source.name, target.boundaries = source.boundaries
    WHEN NOT MATCHED THEN
        INSERT (region_id, country, code, name, boundaries) VALUES (source.region_id, source.country, source.code, source.name, source.boundaries);
END
