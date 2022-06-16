package com.scn.jira.common.ao;

import com.atlassian.activeobjects.external.ActiveObjects;
import net.java.ao.DBParam;
import net.java.ao.Query;
import net.java.ao.RawEntity;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class AbstractRepository<E extends RawEntity<I>, I> {
    @SuppressWarnings("unchecked")
    protected final Class<E> entityClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    @Autowired
    private ActiveObjects ao;

    public void deleteById(@Nonnull I id) {
        delete(findById(id).orElseThrow(IllegalArgumentException::new));
    }

    public void delete(@Nonnull E entity) {
        ao.delete(entity);
    }

    public void deleteAll(@Nonnull Iterable<E> entities) {
        for (E entity : entities) {
            delete(entity);
        }
    }

    public void deleteAll() {
        for (E entity : findAll()) {
            delete(entity);
        }
    }

    public Optional<E> findById(@Nonnull I id) {
        E entity = ao.get(entityClass, id);
        return Optional.ofNullable(entity);
    }

    public boolean existsById(@Nonnull I id) {
        return findById(id).isPresent();
    }

    public List<E> findAll() {
        E[] autoTTs = ao.find(entityClass);
        return Arrays.asList(autoTTs);
    }

    public List<E> findAll(@Nonnull Query query) {
        E[] autoTTs = ao.find(entityClass, query);
        return Arrays.asList(autoTTs);
    }

    public long count(@Nonnull Query query) {
        return ao.count(entityClass, query);
    }

    public long count() {
        return ao.count(entityClass);
    }

    public E create(DBParam... requiredParams) {
        return ao.create(entityClass, requiredParams);
    }

    public E save(@Nonnull E entity) {
        entity.save();
        return entity;
    }

    public void flush() {
        ao.flush();
    }
}
