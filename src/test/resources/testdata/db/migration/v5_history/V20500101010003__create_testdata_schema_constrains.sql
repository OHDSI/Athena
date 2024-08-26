--
-- Name: concept_synonym uq_concept_synonym; Type: CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.concept_synonym
    ADD CONSTRAINT uq_concept_synonym UNIQUE (concept_id, concept_synonym_name, language_concept_id);


--
-- Name: concept xpk_concept; Type: CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.concept
    ADD CONSTRAINT xpk_concept PRIMARY KEY (concept_id);


--
-- Name: concept_ancestor xpk_concept_ancestor; Type: CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.concept_ancestor
    ADD CONSTRAINT xpk_concept_ancestor PRIMARY KEY (ancestor_concept_id, descendant_concept_id);


--
-- Name: concept_class xpk_concept_class; Type: CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.concept_class
    ADD CONSTRAINT xpk_concept_class PRIMARY KEY (concept_class_id);


--
-- Name: concept_relationship xpk_concept_relationship; Type: CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.concept_relationship
    ADD CONSTRAINT xpk_concept_relationship PRIMARY KEY (concept_id_1, concept_id_2, relationship_id);


--
-- Name: domain xpk_domain; Type: CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.domain
    ADD CONSTRAINT xpk_domain PRIMARY KEY (domain_id);


--
-- Name: drug_strength xpk_drug_strength; Type: CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.drug_strength
    ADD CONSTRAINT xpk_drug_strength PRIMARY KEY (drug_concept_id, ingredient_concept_id);


--
-- Name: relationship xpk_relationship; Type: CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.relationship
    ADD CONSTRAINT xpk_relationship PRIMARY KEY (relationship_id);


--
-- Name: vocabulary xpk_vocabulary; Type: CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.vocabulary
    ADD CONSTRAINT xpk_vocabulary PRIMARY KEY (vocabulary_id);


--
-- Name: idx_concept_ancestor_id_1; Type: INDEX; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE INDEX idx_concept_ancestor_id_1 ON vocabulary_testdata.concept_ancestor USING btree (ancestor_concept_id);


--
-- Name: idx_concept_ancestor_id_2; Type: INDEX; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE INDEX idx_concept_ancestor_id_2 ON vocabulary_testdata.concept_ancestor USING btree (descendant_concept_id);


--
-- Name: idx_concept_class_class_id; Type: INDEX; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE UNIQUE INDEX idx_concept_class_class_id ON vocabulary_testdata.concept_class USING btree (concept_class_id);


--
-- Name: idx_concept_class_id; Type: INDEX; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE INDEX idx_concept_class_id ON vocabulary_testdata.concept USING btree (concept_class_id);


--
-- Name: idx_concept_code; Type: INDEX; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE INDEX idx_concept_code ON vocabulary_testdata.concept USING btree (concept_code);


--
-- Name: idx_concept_concept_id; Type: INDEX; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE UNIQUE INDEX idx_concept_concept_id ON vocabulary_testdata.concept USING btree (concept_id);


--
-- Name: idx_concept_domain_id; Type: INDEX; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE INDEX idx_concept_domain_id ON vocabulary_testdata.concept USING btree (domain_id);


--
-- Name: idx_concept_relationship_id_1; Type: INDEX; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE INDEX idx_concept_relationship_id_1 ON vocabulary_testdata.concept_relationship USING btree (concept_id_1);


--
-- Name: idx_concept_relationship_id_2; Type: INDEX; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE INDEX idx_concept_relationship_id_2 ON vocabulary_testdata.concept_relationship USING btree (concept_id_2);


--
-- Name: idx_concept_relationship_id_3; Type: INDEX; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE INDEX idx_concept_relationship_id_3 ON vocabulary_testdata.concept_relationship USING btree (relationship_id);


--
-- Name: idx_concept_synonym_id; Type: INDEX; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE INDEX idx_concept_synonym_id ON vocabulary_testdata.concept_synonym USING btree (concept_id);


--
-- Name: idx_concept_vocabluary_id; Type: INDEX; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE INDEX idx_concept_vocabluary_id ON vocabulary_testdata.concept USING btree (vocabulary_id);


--
-- Name: idx_domain_domain_id; Type: INDEX; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE UNIQUE INDEX idx_domain_domain_id ON vocabulary_testdata.domain USING btree (domain_id);


--
-- Name: idx_drug_strength_id_1; Type: INDEX; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE INDEX idx_drug_strength_id_1 ON vocabulary_testdata.drug_strength USING btree (drug_concept_id);


--
-- Name: idx_drug_strength_id_2; Type: INDEX; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE INDEX idx_drug_strength_id_2 ON vocabulary_testdata.drug_strength USING btree (ingredient_concept_id);


--
-- Name: idx_relationship_rel_id; Type: INDEX; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE UNIQUE INDEX idx_relationship_rel_id ON vocabulary_testdata.relationship USING btree (relationship_id);


--
-- Name: idx_vocabulary_vocabulary_id; Type: INDEX; Schema: vocabulary_testdata; Owner: ohdsi
--

CREATE UNIQUE INDEX idx_vocabulary_vocabulary_id ON vocabulary_testdata.vocabulary USING btree (vocabulary_id);


--
-- Name: concept_ancestor fpk_concept_ancestor_concept_1; Type: FK CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.concept_ancestor
    ADD CONSTRAINT fpk_concept_ancestor_concept_1 FOREIGN KEY (ancestor_concept_id) REFERENCES vocabulary_testdata.concept(concept_id);


--
-- Name: concept_ancestor fpk_concept_ancestor_concept_2; Type: FK CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.concept_ancestor
    ADD CONSTRAINT fpk_concept_ancestor_concept_2 FOREIGN KEY (descendant_concept_id) REFERENCES vocabulary_testdata.concept(concept_id);


--
-- Name: concept fpk_concept_class; Type: FK CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.concept
    ADD CONSTRAINT fpk_concept_class FOREIGN KEY (concept_class_id) REFERENCES vocabulary_testdata.concept_class(concept_class_id);


--
-- Name: concept fpk_concept_domain; Type: FK CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.concept
    ADD CONSTRAINT fpk_concept_domain FOREIGN KEY (domain_id) REFERENCES vocabulary_testdata.domain(domain_id);


--
-- Name: concept_relationship fpk_concept_relationship_c_1; Type: FK CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.concept_relationship
    ADD CONSTRAINT fpk_concept_relationship_c_1 FOREIGN KEY (concept_id_1) REFERENCES vocabulary_testdata.concept(concept_id);


--
-- Name: concept_relationship fpk_concept_relationship_c_2; Type: FK CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.concept_relationship
    ADD CONSTRAINT fpk_concept_relationship_c_2 FOREIGN KEY (concept_id_2) REFERENCES vocabulary_testdata.concept(concept_id);


--
-- Name: concept_relationship fpk_concept_relationship_id; Type: FK CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.concept_relationship
    ADD CONSTRAINT fpk_concept_relationship_id FOREIGN KEY (relationship_id) REFERENCES vocabulary_testdata.relationship(relationship_id);


--
-- Name: concept_synonym fpk_concept_synonym_concept; Type: FK CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.concept_synonym
    ADD CONSTRAINT fpk_concept_synonym_concept FOREIGN KEY (concept_id) REFERENCES vocabulary_testdata.concept(concept_id);


--
-- Name: concept_synonym fpk_concept_synonym_language; Type: FK CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.concept_synonym
    ADD CONSTRAINT fpk_concept_synonym_language FOREIGN KEY (language_concept_id) REFERENCES vocabulary_testdata.concept(concept_id);


--
-- Name: concept fpk_concept_vocabulary; Type: FK CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.concept
    ADD CONSTRAINT fpk_concept_vocabulary FOREIGN KEY (vocabulary_id) REFERENCES vocabulary_testdata.vocabulary(vocabulary_id);


--
-- Name: drug_strength fpk_drug_strength_concept_1; Type: FK CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.drug_strength
    ADD CONSTRAINT fpk_drug_strength_concept_1 FOREIGN KEY (drug_concept_id) REFERENCES vocabulary_testdata.concept(concept_id);


--
-- Name: drug_strength fpk_drug_strength_concept_2; Type: FK CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.drug_strength
    ADD CONSTRAINT fpk_drug_strength_concept_2 FOREIGN KEY (ingredient_concept_id) REFERENCES vocabulary_testdata.concept(concept_id);


--
-- Name: drug_strength fpk_drug_strength_unit_1; Type: FK CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.drug_strength
    ADD CONSTRAINT fpk_drug_strength_unit_1 FOREIGN KEY (amount_unit_concept_id) REFERENCES vocabulary_testdata.concept(concept_id);


--
-- Name: drug_strength fpk_drug_strength_unit_2; Type: FK CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.drug_strength
    ADD CONSTRAINT fpk_drug_strength_unit_2 FOREIGN KEY (numerator_unit_concept_id) REFERENCES vocabulary_testdata.concept(concept_id);


--
-- Name: drug_strength fpk_drug_strength_unit_3; Type: FK CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.drug_strength
    ADD CONSTRAINT fpk_drug_strength_unit_3 FOREIGN KEY (denominator_unit_concept_id) REFERENCES vocabulary_testdata.concept(concept_id);


--
-- Name: relationship fpk_relationship_concept; Type: FK CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.relationship
    ADD CONSTRAINT fpk_relationship_concept FOREIGN KEY (relationship_concept_id) REFERENCES vocabulary_testdata.concept(concept_id);


--
-- Name: relationship fpk_relationship_reverse; Type: FK CONSTRAINT; Schema: vocabulary_testdata; Owner: ohdsi
--

ALTER TABLE ONLY vocabulary_testdata.relationship
    ADD CONSTRAINT fpk_relationship_reverse FOREIGN KEY (reverse_relationship_id) REFERENCES vocabulary_testdata.relationship(relationship_id);


--
-- PostgreSQL database dump complete
--

