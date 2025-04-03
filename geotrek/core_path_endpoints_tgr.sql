create trigger core_path_endpoints_tgr after
    insert
    or
update
    on
    public.core_path for each row execute function update_path_endpoints()