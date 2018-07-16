insert into version values (1);
create sequence hibernate_sequence start with 1 increment by 1;
create table classification (id bigint not null, classification varchar(255), primary key (id));
create table email_message (id bigint not null,classification_id bigint,email_sender  varchar(255),email_to  varchar(255),body clob,subject  varchar(255),send_date  varchar(255), primary key (id));
