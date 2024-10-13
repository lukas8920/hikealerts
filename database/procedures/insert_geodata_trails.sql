CREATE PROCEDURE InsertGeodataTrails
	@trail_id BIGINT,
	@country VARCHAR(2),
	@trailname NVARCHAR(MAX),
	@maplabel NVARCHAR(MAX),
	@unitcode NVARCHAR(MAX),
	@unitname NVARCHAR(MAX),
	@regioncode NVARCHAR(MAX),
	@maintainer NVARCHAR(MAX),
    @lineString NVARCHAR(MAX)
AS
BEGIN
	MERGE INTO dbo.geodata_trails AS target
	USING (SELECT @trail_id as trail_id, @country as country, @trailname as trailname, @maplabel as maplabel, @unitcode as unitcode, @unitname as unitname, @regioncode as regioncode, @maintainer as maintainer, geometry::STGeomFromText(@lineString, 4326) AS coordinates) AS source
	ON target.trail_id = source.trail_id AND target.country = source.country 
	WHEN MATCHED THEN
    	UPDATE SET target.country = source.country, target.trailname = source.trailname, target.maplabel = source.maplabel, target.unitcode = source.unitcode, target.unitname = source.unitname, target.regioncode = source.regioncode, target.maintainer = source.maintainer, target.coordinates = source.coordinates
	WHEN NOT MATCHED THEN
    	INSERT (trail_id, country, trailname, maplabel, unitcode, unitname, regioncode, maintainer, coordinates) VALUES (source.trail_id, source.country, source.trailname, source.maplabel, source.unitcode, source.unitname, source.regioncode, source.maintainer, source.coordinates);
END
