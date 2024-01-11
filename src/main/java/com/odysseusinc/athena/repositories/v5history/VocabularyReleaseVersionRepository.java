package com.odysseusinc.athena.repositories.v5history;

import com.odysseusinc.athena.model.athenav5history.VocabularyReleaseVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VocabularyReleaseVersionRepository extends JpaRepository<VocabularyReleaseVersion, Integer> {

}
