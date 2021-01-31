package com.scn.jira.worklog.core.lazyloading;

import com.atlassian.util.concurrent.LazyReference;

public class LazyLoadingCache<D> {
    private final CacheLoader<D> cacheLoader;
    private volatile LazyLoadingCache<D>.DataReference reference;

    public LazyLoadingCache(CacheLoader<D> cacheLoader) {
        this.cacheLoader = cacheLoader;
        reset();
    }

    public D getData() {
        return this.reference.get();
    }

    public synchronized void reload() {
        LazyLoadingCache<D>.DataReference tempReference = new DataReference();

        tempReference.get();

        this.reference = tempReference;
    }

    public void reset() {
        this.reference = new DataReference();
    }

    public interface CacheLoader<D> {
        D loadData();
    }

    private class DataReference extends LazyReference<D> {
        private DataReference() {
        }

        protected D create() throws Exception {
            return LazyLoadingCache.this.cacheLoader.loadData();
        }
    }
}
