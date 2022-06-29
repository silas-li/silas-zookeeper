package org.example.config;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZKUtils {

    private static ZooKeeper zk;

    private static String address = "10.255.72.158:2181,10.255.72.211:2181,10.255.72.212:2181,10.255.72.213:2181/testConf";

    private static DefaultWatcher watcher = new DefaultWatcher();

    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static ZooKeeper getZk(){
        try {
            zk = new ZooKeeper(address,1000,watcher);
            watcher.setCc(countDownLatch);
            countDownLatch.await();
        } catch (IOException | InterruptedException e){
            e.printStackTrace();
        }

        return zk;
    }
}
