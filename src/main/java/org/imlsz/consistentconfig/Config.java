package org.imlsz.consistentconfig;

/**
 * Created by lsz on 2017/5/12.
 */
public interface Config {
    void save(String key,String val) throws Exception;
    Object get(String key);

}
