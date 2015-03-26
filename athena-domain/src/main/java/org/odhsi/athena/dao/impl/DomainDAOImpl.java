package org.odhsi.athena.dao.impl;

import org.hibernate.SessionFactory;
import org.odhsi.athena.dao.DomainDAO;
import org.odhsi.athena.entity.Domain;

/**
 * Created by GMalikov on 26.03.2015.
 */
public class DomainDAOImpl extends GenericDAOImpl<Domain> implements DomainDAO{

    private SessionFactory sessionFactory;
    public DomainDAOImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

}
