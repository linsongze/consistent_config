package org.imlsz.consistentconfig.lock;

import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;

/**
 * Created by lsz on 2017/5/15.
 */
public class ZkDistributedRwLock implements DistributedRwLock{
    private InterProcessReadWriteLock readWriteLock;
    private Lock readLock = new Lock() {
        @Override
        public void acquire() throws Exception {
            readWriteLock.readLock().acquire();
        }

        @Override
        public void release() throws Exception {
            readWriteLock.readLock().release();
        }
    };
    private Lock writeLock = new Lock() {
        @Override
        public void acquire() throws Exception {
            readWriteLock.writeLock().acquire();
        }
        @Override
        public void release() throws Exception {
            readWriteLock.writeLock().release();
        }
    };
    public ZkDistributedRwLock(InterProcessReadWriteLock readWriteLock){
       this.readWriteLock = readWriteLock;
    }
    @Override
    public Lock readLock() {
        return readLock;
    }

    @Override
    public Lock writeLock() {
        return writeLock;
    }
}
