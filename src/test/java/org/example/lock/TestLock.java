package org.example.lock;

import org.apache.zookeeper.ZooKeeper;
import org.example.config.ZKUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestLock {

    ZooKeeper zk;


    @Before
    public void conn(){
        zk = ZKUtils.getZk();
    }

    @After
    public void close(){
        try {
            zk.close();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void lock(){
        for (int i = 0; i < 10; i++) {
            new Thread(()->{
                String threadName = Thread.currentThread().getName();
                WatcherCallBack watcherCallBack = new WatcherCallBack();
                watcherCallBack.setZk(zk);
                watcherCallBack.setThreadName(threadName);
                //抢锁
                watcherCallBack.tryLock();

                //干活
                System.out.println(threadName+ " working......");
                /*
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                 */


                //释放锁
                watcherCallBack.unLock();

            }).start();
        }

        while(true){

        }
    }
}
