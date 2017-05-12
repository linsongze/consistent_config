package org.imlsz.consistentconfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lsz on 2017/5/12.
 */
public class HashMapConfig implements Config {
    private Map<String,String> storeMap = new HashMap<String, String>();
    public void save(String key, String val) {
        storeMap.put(key,val);
    }

    public String get(String key) {
        String val = storeMap.get(key);
        return val;
    }
}
