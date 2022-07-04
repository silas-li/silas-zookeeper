package org.example.config;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

public class WatcherCallBack implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback{

    ZooKeeper zk;

    MyConf myConf;

    CountDownLatch cc = new CountDownLatch(1);

    public MyConf getMyConf() {
        return myConf;
    }

    public void setMyConf(MyConf myConf) {
        this.myConf = myConf;
    }

    public ZooKeeper getZk() {
        return zk;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    public void aWait() {
        zk.exists("/AppConf",this,this ,"ABC");
        try {
            cc.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void process(WatchedEvent event) {
        switch(event.getType()){
            case None:
                break;
            case NodeCreated:
                zk.getData("/AppConf",this,this,"ABC");
                break;
            case NodeDeleted:
                //节点删除 容忍性
                myConf.setConf("");//清空
                cc = new CountDownLatch(1);//重新计数
                break;
            case NodeDataChanged:
                zk.getData("/AppConf",this,this,"ABC");
                break;
            case NodeChildrenChanged:
                break;
            case DataWatchRemoved:
                break;
            case ChildWatchRemoved:
                break;
            case PersistentWatchRemoved:
                break;
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
        if(data!=null){
            String s = new String(data);
            myConf.setConf(s);
            cc.countDown();
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if(stat!=null){
            zk.getData("/AppConf",this,this,"ABC");
        }
    }


}
