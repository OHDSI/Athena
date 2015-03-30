package org.odhsi.athena.dao.impl;

import org.hibernate.SessionFactory;
import org.odhsi.athena.dao.CohortDefinitionDAO;
import org.odhsi.athena.entity.CohortDefinition;

/**
 * Created by GMalikov on 30.03.2015.
 */
public class CohortDefinitionDAOImpl extends GenericDAOImpl<CohortDefinition> implements CohortDefinitionDAO{

    public CohortDefinitionDAOImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
}
