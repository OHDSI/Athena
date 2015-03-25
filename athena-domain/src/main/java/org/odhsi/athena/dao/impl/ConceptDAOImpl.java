package org.odhsi.athena.dao.impl;

import org.hibernate.SessionFactory;
import org.odhsi.athena.dao.ConceptDAO;
import org.odhsi.athena.entity.Concept;

/**
 * Created by GMalikov on 25.03.2015.
 */
public class ConceptDAOImpl extends GenericDAOImpl<Concept> implements ConceptDAO{

    public ConceptDAOImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
}
