CREATE PROCEDURE GetTrailsAfterOffset
    @Offset INT,
    @Limit INT
AS
BEGIN
SELECT TOP(@Limit) g.id, g.trail_id, g.trailname, g.country, g.maplabel, g.unitcode, g.unitname, g.regioncode, g.maintainer, g.coordinates.STAsBinary() as coordinates
FROM geodata_trails g
WHERE g.id >= @Offset AND g.id IN (
    SELECT trail_ids
    FROM events_trail_ids
    GROUP BY trail_ids
)
ORDER BY g.id;
END;