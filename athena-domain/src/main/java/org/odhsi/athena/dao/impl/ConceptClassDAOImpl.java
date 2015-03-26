package org.odhsi.athena.dao.impl;

import org.hibernate.SessionFactory;
import org.odhsi.athena.dao.ConceptClassDAO;
import org.odhsi.athena.entity.ConceptClass;

/**
 * Created by GMalikov on 26.03.2015.
 */
public class ConceptClassDAOImpl extends GenericDAOImpl<ConceptClass> implements ConceptClassDAO{

    public ConceptClassDAOImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
}
