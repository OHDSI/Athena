package org.odhsi.athena.dao.impl;

import org.hibernate.SessionFactory;
import org.odhsi.athena.dao.AttributeDefinitionDAO;
import org.odhsi.athena.entity.AttributeDefinition;

/**
 * Created by GMalikov on 30.03.2015.
 */
public class AttributeDefinitionDAOImpl extends GenericDAOImpl<AttributeDefinition> implements AttributeDefinitionDAO{

    public AttributeDefinitionDAOImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

}
