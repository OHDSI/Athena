CREATE TABLE concept_history
(
    concept_id            bigint,
    concept_name          varchar(255),
    domain_id             varchar(20),
    vocabulary_id         varchar(20),
    vocabulary_history_id integer,
    concept_class_id      varchar(20),
    standard_concept      varchar(1),
    concept_code          varchar(50),
    valid_start_date      date,
    valid_end_date        date,
    invalid_reason        varchar(1),
    version               integer
) PARTITION BY LIST (version);

CREATE TABLE concept_ancestor_history
(
    ancestor_concept_id      bigint,
    descendant_concept_id    bigint,
    min_levels_of_separation bigint,
    max_levels_of_separation bigint,
    ancestor_vocabulary_history_id   integer,
    descendant_vocabulary_history_id integer,
    version                  integer
) PARTITION BY LIST (version);

CREATE TABLE concept_class_history
(
    concept_class_id         varchar(20),
    concept_class_name       varchar(255),
    concept_class_concept_id numeric(38),
    version                  integer
) PARTITION BY LIST (version);

CREATE TABLE concept_relationship_history
(
    concept_id_1             bigint,
    concept_id_2             bigint,
    relationship_id          varchar(20),
    reverse_relationship_id  varchar(20),
    valid_start_date         date,
    reverse_valid_start_date date,
    valid_end_date           date,
    invalid_reason           varchar(1),
    vocabulary_history_id_1  integer,
    vocabulary_history_id_2  integer,
    version                  integer
) PARTITION BY LIST (version);

CREATE TABLE concept_synonym_history
(
    concept_id            bigint,
    concept_synonym_name  varchar(1000),
    language_concept_id   bigint,
    vocabulary_history_id integer,
    version               integer
) PARTITION BY LIST (version);

create table drug_strength_history
(
    drug_concept_id             bigint,
    ingredient_concept_id       bigint,
    amount_value                numeric,
    amount_unit_concept_id      bigint,
    numerator_value             numeric,
    numerator_unit_concept_id   bigint,
    denominator_value           numeric,
    denominator_unit_concept_id bigint,
    box_size                    integer,
    valid_start_date            date,
    valid_end_date              date,
    invalid_reason              varchar(1),
    vocabulary_history_id       integer,
    version                     integer
) PARTITION BY LIST (version);

CREATE TABLE concept_versions_history
(
    conept_id    integer,
    concept_name text,
    is_new       boolean,
    is_deleted   boolean,
    version_from integer,
    version_to   integer,
    version      integer
) PARTITION BY LIST (version);

CREATE TABLE domain_history
(
    domain_id         varchar(20),
    domain_name       varchar(255),
    domain_concept_id bigint,
    version           integer
) PARTITION BY LIST (version);

CREATE TABLE relationship_history
(
    relationship_id         varchar(20),
    relationship_name       varchar(255),
    is_hierarchical         varchar(1),
    defines_ancestry        varchar(1),
    reverse_relationship_id varchar(20),
    relationship_concept_id bigint,
    version                 integer
) PARTITION BY LIST (version);

CREATE TABLE vocabulary_history
(
    vocabulary_history_id integer,
    vocabulary_id         varchar(20),
    vocabulary_name       varchar(255),
    vocabulary_reference  varchar(255),
    vocabulary_version    varchar(255),
    vocabulary_concept_id bigint,
    version               integer
) PARTITION BY LIST (version);