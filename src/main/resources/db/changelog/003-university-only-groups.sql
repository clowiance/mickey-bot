--liquibase formatted sql

--changeset mickey:007-university-only-study-groups splitStatements:true
DELETE FROM managed_study_groups
 WHERE category IS NULL OR category <> 'UNIVERSITY';
