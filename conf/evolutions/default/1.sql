# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "comments" ("id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,"createdAt" TIMESTAMP,"updatedAt" TIMESTAMP,"topic_id" BIGINT NOT NULL,"content" VARCHAR NOT NULL);

# --- !Downs

drop table "comments";

