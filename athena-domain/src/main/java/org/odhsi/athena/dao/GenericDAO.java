package org.odhsi.athena.dao;

import java.util.List;

/**
 * Created by GMalikov on 25.03.2015.
 */
public interface GenericDAO<T> {

    public void delete(T item);

    public void save(T item);

    public T find(Long id);

    public List<T> findAll();

    public void deleteAll();

    public void saveAll(List<T> item);
}
