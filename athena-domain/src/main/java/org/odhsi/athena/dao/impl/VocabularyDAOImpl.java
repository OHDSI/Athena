package org.odhsi.athena.dao.impl;

import org.hibernate.SessionFactory;
import org.odhsi.athena.dao.VocabularyDAO;
import org.odhsi.athena.entity.Vocabulary;

/**
 * Created by GMalikov on 26.03.2015.
 */

public class VocabularyDAOImpl extends GenericDAOImpl<Vocabulary> implements VocabularyDAO{

    public VocabularyDAOImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

}
