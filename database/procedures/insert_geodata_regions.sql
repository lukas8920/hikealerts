CREATE PROCEDURE InsertGeodataRegions
	@id VARCHAR(10),
	@code VARCHAR(10),
	@name NVARCHAR(MAX),
    @polygon NVARCHAR(MAX)
AS
BEGIN
	MERGE INTO dbo.us_geodata_regions AS target
	USING (SELECT @id as id, @code as code, @name as name, geometry::STGeomFromText(@polygon, 4326) AS boundaries) AS source
	ON target.id = source.id
	WHEN MATCHED THEN
    	UPDATE SET target.code = source.code, target.name = source.name, target.boundaries = source.boundaries
	WHEN NOT MATCHED THEN
    	INSERT (id, code, name, boundaries) VALUES (source.id, source.code, source.name, source.boundaries);
END
