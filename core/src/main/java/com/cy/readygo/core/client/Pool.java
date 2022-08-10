package com.cy.readygo.core.client;

import com.cy.readygo.core.exception.RedisXConnectionException;
import com.cy.readygo.core.exception.RedisXException;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.Closeable;
import java.util.NoSuchElementException;


public abstract class Pool<T> implements Closeable {
        protected GenericObjectPool<T> internalPool;

        public Pool() {
        }

        public Pool(GenericObjectPoolConfig poolConfig, PooledObjectFactory<T> factory) {
            this.initPool(poolConfig, factory);
        }

        public void close() {
            this.destroy();
        }

        public boolean isClosed() {
            return this.internalPool.isClosed();
        }

        public void initPool(GenericObjectPoolConfig poolConfig, PooledObjectFactory<T> factory) {
            if (this.internalPool != null) {
                try {
                    this.closeInternalPool();
                } catch (Exception var4) {
                }
            }

            this.internalPool = new GenericObjectPool(factory, poolConfig);
        }

        public T getResource() {
            try {
                return this.internalPool.borrowObject();
            } catch (NoSuchElementException var2) {
                if (null == var2.getCause()) {
                    throw new RedisXConnectionException("Could not get a resource since the pool is exhausted", var2);
                } else {
                    throw new RedisXException("Could not get a resource from the pool", var2);
                }
            } catch (Exception var3) {
                throw new RedisXConnectionException("Could not get a resource from the pool", var3);
            }
        }

        protected void returnResourceObject(T resource) {
            if (resource != null) {
                try {
                    this.internalPool.returnObject(resource);
                } catch (Exception var3) {
                    throw new RedisXException("Could not return the resource to the pool", var3);
                }
            }
        }

        protected void returnBrokenResource(T resource) {
            if (resource != null) {
                this.returnBrokenResourceObject(resource);
            }

        }

        protected void returnResource(T resource) {
            if (resource != null) {
                this.returnResourceObject(resource);
            }

        }

        public void destroy() {
            this.closeInternalPool();
        }

        protected void returnBrokenResourceObject(T resource) {
            try {
                this.internalPool.invalidateObject(resource);
            } catch (Exception var3) {
                throw new RedisXException("Could not return the broken resource to the pool", var3);
            }
        }

        protected void closeInternalPool() {
            try {
                this.internalPool.close();
            } catch (Exception var2) {
                throw new RedisXException("Could not destroy the pool", var2);
            }
        }

        public int getNumActive() {
            return this.poolInactive() ? -1 : this.internalPool.getNumActive();
        }

        public int getNumIdle() {
            return this.poolInactive() ? -1 : this.internalPool.getNumIdle();
        }

        public int getNumWaiters() {
            return this.poolInactive() ? -1 : this.internalPool.getNumWaiters();
        }

        public long getMeanBorrowWaitTimeMillis() {
            return this.poolInactive() ? -1L : this.internalPool.getMeanBorrowWaitTimeMillis();
        }

        public long getMaxBorrowWaitTimeMillis() {
            return this.poolInactive() ? -1L : this.internalPool.getMaxBorrowWaitTimeMillis();
        }

        private boolean poolInactive() {
            return this.internalPool == null || this.internalPool.isClosed();
        }

        public void addObjects(int count) {
            try {
                for(int i = 0; i < count; ++i) {
                    this.internalPool.addObject();
                }

            } catch (Exception var3) {
                throw new RedisXException("Error trying to add idle objects", var3);
            }
        }
}
