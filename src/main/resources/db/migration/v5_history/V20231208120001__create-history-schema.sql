CREATE TABLE public.concept_history
(
    concept_id       bigint,
    concept_name     varchar(255) not null,
    domain_id        varchar(20)  not null,
    vocabulary_id    varchar(20)  not null,
    concept_class_id varchar(20)  not null,
    standard_concept varchar(1),
    concept_code     varchar(50)  not null,
    valid_start_date date         not null,
    valid_end_date   date         not null,
    invalid_reason   varchar(1),
    version          INT
) PARTITION BY LIST (version);

CREATE TABLE public.concept_ancestor_history
(
    ancestor_concept_id      bigint      not null,
    descendant_concept_id    bigint      not null,
    min_levels_of_separation bigint      not null,
    max_levels_of_separation bigint      not null,
    ancestor_vocabulary_id   varchar(20) not null,
    descendant_vocabulary_id varchar(20) not null,
    version                  INT
) PARTITION BY LIST (version);

CREATE TABLE public.concept_class_history
(
    concept_class_id         varchar(20)  not null,
    concept_class_name       varchar(255) not null,
    concept_class_concept_id numeric(38)  not null,
    version                  INT
) PARTITION BY LIST (version);

CREATE TABLE public.concept_relationship_history
(
    concept_id_1     bigint      not null,
    concept_id_2     bigint      not null,
    relationship_id  varchar(20) not null,
    reverse_relationship_id  varchar(20) not null,
    valid_start_date date        not null,
    valid_end_date   date        not null,
    invalid_reason   varchar(1),
    vocabulary_id_1  varchar(20) not null,
    vocabulary_id_2  varchar(20) not null,
    version          INT
) PARTITION BY LIST (version);

CREATE TABLE concept_synonym_history
(
    concept_id           bigint        not null,
    concept_synonym_name varchar(1000) not null,
    language_concept_id  bigint        not null,
    vocabulary_id        varchar(20)   not null,
    version              INT
) PARTITION BY LIST (version);

create table drug_strength_history
(
    drug_concept_id             bigint      not null,
    ingredient_concept_id       bigint      not null,
    amount_value                numeric,
    amount_unit_concept_id      bigint,
    numerator_value             numeric,
    numerator_unit_concept_id   bigint,
    denominator_value           numeric,
    denominator_unit_concept_id bigint,
    box_size                    integer,
    valid_start_date            date        not null,
    valid_end_date              date        not null,
    invalid_reason              varchar(1),
    vocabulary_id               varchar(20) not null,
    version                     INT
) PARTITION BY LIST (version);

CREATE TABLE concept_versions_history
(
    conept_id    integer,
    concept_name text,
    is_new       boolean,
    is_deleted   boolean,
    version_from integer,
    version_to   integer,
    version      INT
) PARTITION BY LIST (version);

CREATE TABLE domain_history
(
    domain_id         varchar(20)  not null,
    domain_name       varchar(255) not null,
    domain_concept_id bigint       not null,
    version           INT
) PARTITION BY LIST (version);

CREATE TABLE relationship_history
(
    relationship_id         varchar(20)  not null,
    relationship_name       varchar(255) not null,
    is_hierarchical         varchar(1)   not null,
    defines_ancestry        varchar(1)   not null,
    reverse_relationship_id varchar(20)  not null,
    relationship_concept_id bigint       not null,
    version                 INT
) PARTITION BY LIST (version);

CREATE TABLE vocabulary_history
(
    vocabulary_id         varchar(20)  not null,
    vocabulary_name       varchar(255) not null,
    vocabulary_reference  varchar(255),
    vocabulary_version    varchar(255),
    vocabulary_concept_id bigint       not null,
    version               INT
) PARTITION BY LIST (version);