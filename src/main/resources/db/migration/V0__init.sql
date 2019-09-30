
create table project (id uuid not null, created timestamp, description varchar(10485760), modified timestamp, name varchar(10485760), requirement varchar(10485760), short_description varchar(10485760), status int4, supervisor_name varchar(10485760), primary key (id));
create table project_tags (project_id uuid not null, tag_name varchar(10485760));
alter table project_tags add constraint FKfvy64usu7e9x7ev6obh91q0qe foreign key (project_id) references project;
