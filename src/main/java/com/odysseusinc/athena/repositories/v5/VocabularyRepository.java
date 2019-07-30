package com.odysseusinc.athena.repositories.v5;

import com.odysseusinc.athena.model.athenav5.VocabularyV5;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface VocabularyRepository extends JpaRepository<VocabularyV5, String> {

}
