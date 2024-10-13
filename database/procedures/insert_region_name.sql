CREATE PROCEDURE InsertRegionName 
	@code VARCHAR(10),
	@name VARCHAR(MAX)
AS
BEGIN
	MERGE INTO dbo.us_geodata_regions AS target
	USING (SELECT @code as code, @name as name) AS source
	ON target.code = UPPER(source.code)
	WHEN MATCHED THEN
    	UPDATE SET target.name = source.name;
END
