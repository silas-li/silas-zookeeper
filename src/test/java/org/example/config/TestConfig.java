package org.example.config;

import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestConfig {

    ZooKeeper zk;


    @Before
    public void conn(){
        zk = ZKUtils.getZk();
    }

    @Test
    public void getConf(){
        WactherCallBack wactherCallBack = new WactherCallBack();
        wactherCallBack.setZk(zk);
        MyConf myConf = new MyConf();
        wactherCallBack.setMyConf(myConf);

        wactherCallBack.aWait();

        //1.节点不存在
        //2.节点存在

        while (true){
            if("".equals(myConf.getConf())){
                System.out.println("conf 丢失......");
                wactherCallBack.aWait();
            }else{
                System.out.println(myConf.getConf());
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
