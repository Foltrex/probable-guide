-- worklog type table migration
create table if not exists public."AO_0B2A23_WLTYPE"
as table public.worklogtype_scn;

alter table if exists public."AO_0B2A23_WLTYPE"
    rename column id to "ID";

alter table if exists public."AO_0B2A23_WLTYPE"
    alter column "ID" type int8 USING "ID"::int8;

alter table if exists public."AO_0B2A23_WLTYPE"
    add primary key ("ID");

alter table if exists public."AO_0B2A23_WLTYPE"
    rename column sequence to "SEQUENCE";

alter table if exists public."AO_0B2A23_WLTYPE"
    rename column pname to "NAME";

alter table if exists public."AO_0B2A23_WLTYPE"
    rename column description to "DESCRIPTION";

alter table if exists public."AO_0B2A23_WLTYPE"
    rename column iconurl to "ICON_URI";

alter table if exists public."AO_0B2A23_WLTYPE"
    rename column status_color to "STATUS_COLOR";

-- scn worklog table migration
create table if not exists public."AO_0B2A23_SCN_WORKLOG"
as table public.worklog_scn;

alter table if exists public."AO_0B2A23_SCN_WORKLOG"
    rename column id to "ID";

alter table if exists public."AO_0B2A23_SCN_WORKLOG"
    add primary key ("ID");

alter table if exists public."AO_0B2A23_SCN_WORKLOG"
    rename column worklog_id to "WORKLOG_ID";

alter table if exists public."AO_0B2A23_SCN_WORKLOG"
    rename column issueid to "ISSUE_ID";

alter table if exists public."AO_0B2A23_SCN_WORKLOG"
    rename column author to "AUTHOR_KEY";

alter table if exists public."AO_0B2A23_SCN_WORKLOG"
    rename column grouplevel to "GROUP_LEVEL";

alter table if exists public."AO_0B2A23_SCN_WORKLOG"
    rename column rolelevel to "ROLE_LEVEL_ID";

alter table if exists public."AO_0B2A23_SCN_WORKLOG"
    rename column worklogbody to "COMMENT";

alter table if exists public."AO_0B2A23_SCN_WORKLOG"
    rename column created to "CREATED";

alter table if exists public."AO_0B2A23_SCN_WORKLOG"
    rename column updateauthor to "UPDATE_AUTHOR_KEY";

alter table if exists public."AO_0B2A23_SCN_WORKLOG"
    rename column updated to "UPDATED";

alter table if exists public."AO_0B2A23_SCN_WORKLOG"
    rename column startdate to "START_DATE";

alter table if exists public."AO_0B2A23_SCN_WORKLOG"
    rename column timeworked to "TIME_SPENT";

alter table if exists public."AO_0B2A23_SCN_WORKLOG"
    rename column worklogtype to "WORKLOG_TYPE_ID";

alter table if exists public."AO_0B2A23_SCN_WORKLOG"
    alter column "WORKLOG_TYPE_ID" type int8 using "WORKLOG_TYPE_ID"::int8,
    add foreign key ("WORKLOG_TYPE_ID") references public."AO_0B2A23_WLTYPE";

-- scn issue time table migration
create table if not exists public."AO_0B2A23_SCN_ISSUE_TIME"
as table public.jiraissue_extended_scn;

alter table if exists public."AO_0B2A23_SCN_ISSUE_TIME"
    drop column if exists id;

alter table if exists public."AO_0B2A23_SCN_ISSUE_TIME"
    rename column issue_id to "ISSUE_ID";

alter table if exists public."AO_0B2A23_SCN_ISSUE_TIME"
    add primary key ("ISSUE_ID");

alter table if exists public."AO_0B2A23_SCN_ISSUE_TIME"
    rename column timeoriginalestimate to "ORIGINAL_ESTIMATE";

alter table if exists public."AO_0B2A23_SCN_ISSUE_TIME"
    rename column timeestimate to "ESTIMATE";

alter table if exists public."AO_0B2A23_SCN_ISSUE_TIME"
    rename column timespent to "TIME_SPENT";

-- scn issue time table migration
create table if not exists public."AO_0B2A23_WORKLOG_WLTYPE"
as table public.worklog_worklogtype_scn;

alter table if exists public."AO_0B2A23_WORKLOG_WLTYPE"
    rename column id to "WORKLOG_ID";

alter table if exists public."AO_0B2A23_WORKLOG_WLTYPE"
    add primary key ("WORKLOG_ID");

alter table if exists public."AO_0B2A23_WORKLOG_WLTYPE"
    rename column worklogtype to "WORKLOG_TYPE_ID";

alter table if exists public."AO_0B2A23_WORKLOG_WLTYPE"
    alter column "WORKLOG_TYPE_ID" type int8 using "WORKLOG_TYPE_ID"::int8,
    add foreign key ("WORKLOG_TYPE_ID") references public."AO_0B2A23_WLTYPE";
