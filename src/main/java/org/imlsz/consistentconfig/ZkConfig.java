package org.imlsz.consistentconfig;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.imlsz.consistentconfig.lock.DistributedRwLock;
import org.imlsz.consistentconfig.lock.ZkDistributedRwLock;
import org.imlsz.consistentconfig.utils.SerializableUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by lsz on 2017/5/12.
 */
public class ZkConfig implements Config {
    private Map<String, String> storeMap = new HashMap<String, String>();
    private static final String STORE_PATH = "/Zkx";
    private static final String STORE_NAMES_PATH = "/ZkConfigList";
    private static final String STORE_NAMES_Lock_PATH = "/ZkConfigListLock";
    private DistributedRwLock nameslock;
    private CuratorFramework client = null;
    private ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
    private boolean readonly;

    public ZkConfig(String connectStr,String baseDir,boolean readonly){
        this.readonly = readonly;
        client = CuratorFrameworkFactory.builder().connectString(connectStr)
                .namespace(baseDir).retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 1000))
                .connectionTimeoutMs(5000).build();
        client.start();
        try {
            client.blockUntilConnected();
            nameslock = new ZkDistributedRwLock( new InterProcessReadWriteLock(client, STORE_NAMES_Lock_PATH));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public ZkConfig(String connectStr) {
        this(connectStr,"CCconfig",false);
    }

    public ZkConfig setNamesLock(DistributedRwLock distributedRwLock){
        this.nameslock = distributedRwLock;
        return this;
    }
    public ZkConfig init() throws Exception {
        _initWatch();
        _init();
        return this;
    }

    private void _initWatch() throws Exception {
        PathChildrenCache watcher = new PathChildrenCache(
                client,
                STORE_PATH,
                true    // if cache data
        );
        watcher.getListenable().addListener((client1, event) -> {
            try {
                rwlock.writeLock().lock();
                ChildData data = event.getData();
                if (data == null) {
                    System.out.println("No data in event[" + event + "]");
                } else {
                    System.out.println("Receive event: "
                            + "type=[" + event.getType() + "]"
                            + ", path=[" + data.getPath() + "]"
                            + ", data=[" + new String(data.getData()) + "]"
                            + ", stat=[" + data.getStat() + "]");
                    if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED
                            || event.getType() == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
                        String path = data.getPath();
                        if (path.startsWith(STORE_PATH)) {
                            String key = path.replace(STORE_PATH + "/", "");
                            String dataStr = new String(data.getData(), "utf-8");
                            storeMap.put(key, dataStr);
                        }
                    }

                }
            }finally {
                rwlock.writeLock().unlock();
            }
        });
        watcher.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

    }
    private void _init() throws Exception {
        try {
            rwlock.readLock().lock();
            nameslock.readLock().acquire();
            Set<String> names = _getNames();
            for (String str : names) {
                storeMap.put(str, _getDateFromZK(str));
            }
        }finally {
            nameslock.readLock().release();
           rwlock.readLock().unlock();

        }

    }

    private Set<String> _getNames() throws Exception {
        Stat stat = client.checkExists().forPath(STORE_NAMES_PATH);
        if (stat != null) {
            byte[] namesdata = client.getData().forPath(STORE_NAMES_PATH);
            if (namesdata != null && namesdata.length > 0) {
                HashSet<String> names = (HashSet<String>) SerializableUtils.decode(namesdata);
                return names;
            }
        }
        return new HashSet<String>();
    }

    private String _getDateFromZK(String key) throws Exception {
        return new String(client.getData().forPath(getDecKey(key)), "utf-8");
    }

    public String getDecKey(String key) {
        return STORE_PATH + "/" + key;
    }

    public void save(String key, String val) throws Exception {
        if(readonly){
            throw new UnsupportedOperationException("the config client is readonly!");
        }
        try {
            rwlock.writeLock().lock();
            nameslock.writeLock().acquire();
            Set<String> names = _getNames();
            names.add(key);
            storeMap.put(key, val);
            _save(getDecKey(key), val.getBytes("utf-8"));
            _save(STORE_NAMES_PATH, SerializableUtils.encode(names));
        } finally {
            rwlock.writeLock().unlock();
            nameslock.writeLock().release();
        }
    }
    public void _save(String key,byte[] data) throws Exception {
        Stat stat = client.checkExists().forPath(key);
        if (stat != null) {
            client.setData().forPath(key,data);
        }else {
                client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                .forPath(key,data);
        }
    }
    public String get(String key) {
        try {
            rwlock.readLock().lock();
            return storeMap.get(key);
        } finally {
            rwlock.readLock().unlock();
        }
    }
}
