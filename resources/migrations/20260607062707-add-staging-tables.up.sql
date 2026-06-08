CREATE table member_staging (
 slack_id text primary key,
 name text not null,
 display_name text,
 image_192 text
);
--;;
CREATE table channel_staging (
 slack_id text primary key,
 name text not null,
 purpose text,
 topic text
);
--;;
CREATE table message_staging (
 channel_id text,
 member_id text,
 text text,
 ts text,
 created_at timestamptz,
 parent_ts text,
 deleted_ts text
);

--;;
CREATE table reaction_staging (
 member_id text,
 channel_id text,
 message_id text,
 reaction text,
 url text
);
