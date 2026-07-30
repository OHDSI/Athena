--
-- PostgreSQL database dump
--

-- Dumped from database version 16.1 (Debian 16.1-1.pgdg120+1)
-- Dumped by pg_dump version 16.1 (Debian 16.1-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: vocabulary_testdata; Type: SCHEMA; Schema: -; Owner: ohdsi
--

CREATE SCHEMA vocabulary_testdata;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: concept; Type: TABLE; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE TABLE vocabulary_testdata.concept (
    concept_id integer NOT NULL,
    concept_name character varying(255) NOT NULL,
    domain_id character varying(20) NOT NULL,
    vocabulary_id character varying(20) NOT NULL,
    concept_class_id character varying(20) NOT NULL,
    standard_concept character varying(1),
    concept_code character varying(50) NOT NULL,
    valid_start_date date NOT NULL,
    valid_end_date date NOT NULL,
    invalid_reason character varying(1)
);



--
-- Name: concept_ancestor; Type: TABLE; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE TABLE vocabulary_testdata.concept_ancestor (
    ancestor_concept_id integer NOT NULL,
    descendant_concept_id integer NOT NULL,
    min_levels_of_separation integer NOT NULL,
    max_levels_of_separation integer NOT NULL
);



--
-- Name: concept_class; Type: TABLE; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE TABLE vocabulary_testdata.concept_class (
    concept_class_id character varying(20) NOT NULL,
    concept_class_name character varying(255) NOT NULL,
    concept_class_concept_id integer NOT NULL
);



--
-- Name: concept_relationship; Type: TABLE; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE TABLE vocabulary_testdata.concept_relationship (
    concept_id_1 integer NOT NULL,
    concept_id_2 integer NOT NULL,
    relationship_id character varying(20) NOT NULL,
    valid_start_date date NOT NULL,
    valid_end_date date NOT NULL,
    invalid_reason character varying(1)
);



--
-- Name: concept_synonym; Type: TABLE; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE TABLE vocabulary_testdata.concept_synonym (
    concept_id integer NOT NULL,
    concept_synonym_name character varying(1000) NOT NULL,
    language_concept_id integer NOT NULL
);



--
-- Name: domain; Type: TABLE; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE TABLE vocabulary_testdata.domain (
    domain_id character varying(20) NOT NULL,
    domain_name character varying(255) NOT NULL,
    domain_concept_id integer NOT NULL
);



--
-- Name: drug_strength; Type: TABLE; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE TABLE vocabulary_testdata.drug_strength (
    drug_concept_id integer NOT NULL,
    ingredient_concept_id integer NOT NULL,
    amount_value numeric,
    amount_unit_concept_id integer,
    numerator_value numeric,
    numerator_unit_concept_id integer,
    denominator_value numeric,
    denominator_unit_concept_id integer,
    box_size integer,
    valid_start_date date NOT NULL,
    valid_end_date date NOT NULL,
    invalid_reason character varying(1)
);



--
-- Name: relationship; Type: TABLE; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE TABLE vocabulary_testdata.relationship (
    relationship_id character varying(20) NOT NULL,
    relationship_name character varying(255) NOT NULL,
    is_hierarchical character varying(1) NOT NULL,
    defines_ancestry character varying(1) NOT NULL,
    reverse_relationship_id character varying(20) NOT NULL,
    relationship_concept_id integer NOT NULL
);



--
-- Name: vocabulary; Type: TABLE; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE TABLE vocabulary_testdata.vocabulary (
    vocabulary_id character varying(20) NOT NULL,
    vocabulary_name character varying(255) NOT NULL,
    vocabulary_reference character varying(255) NOT NULL,
    vocabulary_version character varying(255),
    vocabulary_concept_id integer NOT NULL
);