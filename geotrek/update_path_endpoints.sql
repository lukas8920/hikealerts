-- DROP FUNCTION public.update_path_endpoints();

CREATE OR REPLACE FUNCTION public.update_path_endpoints()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
DECLARE
    vertices_table TEXT := 'core_path_vertices_pgr';
    new_geom GEOMETRY := NEW.geom;
    tolerance FLOAT := 0.00001;
    srid INT := 3857;
    calc_source_id BIGINT;
    calc_target_id BIGINT;
	var_id BIGINT;
	var_name TEXT;
	var_geom GEOMETRY;
	var_start_source BIGINT;
	var_start_target BIGINT;
BEGIN
	IF NEW.source IS NULL OR NEW.TARGET is NULL OR NEW.geom IS DISTINCT FROM OLD.geom THEN
	    calc_source_id := _pgr_pointToId(ST_StartPoint(new_geom), tolerance, vertices_table, srid);

	    calc_target_id := _pgr_pointToId(ST_EndPoint(new_geom), tolerance, vertices_table, srid);

        select id, name, geom, source, target
        into var_id, var_name, var_geom, var_start_source, var_start_target
        from core_path
        where name = NEW.name and target = calc_target_id and source <> calc_source_id
        limit 1;

        if var_id is not null then
            UPDATE core_path
            SET target = var_start_source
            WHERE id = NEW.id;
        else
            UPDATE core_path
            SET source = calc_source_id, target = calc_target_id
            WHERE id = NEW.id;
        end if;

	    RAISE LOG 'Path updated: Old Id % / Id % / Source % -> Target %', OLD.ID, NEW.id, calc_source_id, calc_target_id;

        RETURN NEW;
    END IF;
    RETURN NULL;
END $function$
;
