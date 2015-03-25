package org.odhsi.athena.dao.impl;

import org.hibernate.*;
import org.odhsi.athena.dao.GenericDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.orm.hibernate4.HibernateTransactionManager;

import java.util.List;

/**
 * Created by GMalikov on 25.03.2015.
 */
public class GenericDAOImpl<T> implements GenericDAO<T> {
    private Session session;
    private Transaction transaction;
    private SessionFactory sessionFactory = null;
    private HibernateTransactionManager transactionManager = null;
    private Class<T> genericType;

    protected final Logger LOGGER = LoggerFactory.getLogger(GenericDAOImpl.class);

    @SuppressWarnings("unchecked")
    public GenericDAOImpl(HibernateTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.genericType = (Class<T>) GenericTypeResolver.resolveTypeArgument(
                getClass(), GenericDAOImpl.class);
    }

    @SuppressWarnings("unchecked")
    public GenericDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.genericType = (Class<T>) GenericTypeResolver.resolveTypeArgument(
                getClass(), GenericDAOImpl.class);
    }

    public void delete(T item) {
        try {
            startOperation();
            session.delete(item);
            transaction.commit();
        } catch (final HibernateException e) {
            transaction.rollback();
            LOGGER.error("Error during delete execution: ", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    public void save(T item) {
        try {
            startOperation();
            session.save(item);
            transaction.commit();
        } catch (final HibernateException e) {
            transaction.rollback();
            LOGGER.error("Error during save execution: ", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public T find(Long id) {
        T result = null;
        try {
            startOperation();
            result = (T) session.load(genericType, id);
            transaction.commit();
        } catch (final HibernateException e) {
            transaction.rollback();
            LOGGER.error("Error during find execution: ", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {

        List<T> result = null;
        try {
            this.genericType = (Class<T>) GenericTypeResolver
                    .resolveTypeArgument(getClass(), GenericDAOImpl.class);
            startOperation();
            final Query query = session.createQuery("from "
                    + genericType.getName());
            result = query.list();
            transaction.commit();
        } catch (final HibernateException e) {
            transaction.rollback();
            LOGGER.error("Error during findAll execution: ", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public void deleteAll() {
        try {
            this.genericType = (Class<T>) GenericTypeResolver
                    .resolveTypeArgument(getClass(), GenericDAOImpl.class);
            startOperation();
            final Query query = session.createQuery("delete from "
                    + genericType.getName());
            query.executeUpdate();
            transaction.commit();
        } catch (final HibernateException e) {
            transaction.rollback();
            LOGGER.error("Error during deleteAll execution: ", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    public void saveAll(List<T> item) {
        try {
            startOperation();
            for (int i = 0; i < item.size(); i++) {
                session.save(item.get(i));
                if (i % 20 == 0) {
                    session.flush();
                    session.clear();
                }
            }
            transaction.commit();
        } catch (final HibernateException e) {
            transaction.rollback();
            LOGGER.error("Error during saveAll execution: ", e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    protected void startOperation() {
        if (sessionFactory != null) {
            session = sessionFactory.getCurrentSession();
        } else {
            session = transactionManager.getSessionFactory().openSession();
        }
        transaction = session.beginTransaction();
    }
}
