package org.imlsz.consistentconfig.lock;

/**
 * Created by lsz on 2017/5/15.
 */
public interface DistributedRwLock {
    Lock readLock();
    Lock writeLock();

}
