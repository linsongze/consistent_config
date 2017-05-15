package org.imlsz.consistentconfig;

import org.junit.Test;

/**
 * Created by lsz on 2017/5/12.
 */
public class ZkConfigTest {
    @Test
    public void test() throws Exception {
        ZkConfig config = new ZkConfig("localhost:2181").init();
        ZkConfig config2 = new ZkConfig("localhost:2181").init();

        new Thread(()->{
            for (int i = 0;i<100;i++){
                   String val =  config2.get("1");
                   String val2 =  config2.get("2");
                   System.out.println(val);
                   System.out.println(val2);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }) .start();

        config.save("1","1");
        Thread.sleep(2000);
        config.save("1","2");
        Thread.sleep(2000);
        config.save("2","3");
        Thread.sleep(2000);
        config.save("1","222");
        Thread.sleep(2000);
        config.save("1","3222");

        Thread.sleep(2000*3000);
    }
}
