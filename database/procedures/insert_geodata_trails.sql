CREATE PROCEDURE InsertGeodataTrails
	@id BIGINT,
	@trailname NVARCHAR(MAX),
	@maplabel NVARCHAR(MAX),
	@unitcode NVARCHAR(MAX),
	@unitname NVARCHAR(MAX),
	@regioncode NVARCHAR(MAX),
	@maintainer NVARCHAR(MAX),
    @lineString NVARCHAR(MAX)
AS
BEGIN
	SET IDENTITY_INSERT us_geodata_trails ON;
	MERGE INTO dbo.us_geodata_trails AS target
	USING (SELECT @id as id, @trailname as trailname, @maplabel as maplabel, @unitcode as unitcode, @unitname as unitname, @regioncode as regioncode, @maintainer as maintainer, geometry::STGeomFromText(@lineString, 4326) AS coordinates) AS source
	ON target.id = source.id
	WHEN MATCHED THEN
    	UPDATE SET target.trailname = source.trailname, target.maplabel = source.maplabel, target.unitcode = source.unitcode, target.unitname = source.unitname, target.regioncode = source.regioncode, target.maintainer = source.maintainer, target.coordinates = source.coordinates
	WHEN NOT MATCHED THEN
    	INSERT (id, trailname, maplabel, unitcode, unitname, regioncode, maintainer, coordinates) VALUES (source.id, source.trailname, source.maplabel, source.unitcode, source.unitname, source.regioncode, source.maintainer, source.coordinates);
   	SET IDENTITY_INSERT us_geodata_trails OFF;
END
