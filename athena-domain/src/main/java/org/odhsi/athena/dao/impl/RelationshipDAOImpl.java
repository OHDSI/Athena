package org.odhsi.athena.dao.impl;

import org.hibernate.SessionFactory;
import org.odhsi.athena.dao.RelationshipDAO;
import org.odhsi.athena.entity.Relationship;

/**
 * Created by GMalikov on 27.03.2015.
 */
public class RelationshipDAOImpl extends GenericDAOImpl<Relationship> implements RelationshipDAO {

    public RelationshipDAOImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
}
