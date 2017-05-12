package org.imlsz.consistentconfig;

import org.junit.Assert;

import java.security.Key;

/**
 * Created by lsz on 2017/5/12.
 */
public class HashMapConfigTest {
    HashMapConfig config = new HashMapConfig();
    @org.junit.Test
    public void saveAndGet(){
        config.save("hello","world");
        Assert.assertEquals("world",config.get("hello"));
    }



}
