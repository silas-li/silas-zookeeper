package org.example.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WatcherCallBack implements Watcher, AsyncCallback.StringCallback, AsyncCallback.ChildrenCallback, AsyncCallback.StatCallback {

    ZooKeeper zk;
    String threadName;
    CountDownLatch cdl = new CountDownLatch(1);
    String pathName;

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public void setZk(ZooKeeper zk) {
        this.zk = zk;
    }

    //加锁
    public void tryLock(){

        try {
            //zk.getData("/",false,new Stat()); 重入锁，比较线程名字，如果是同一个名字 直接获得锁
            //每个线程都能创建临时节点成功
            zk.create("/lock",threadName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL,this,"ABC");
            cdl.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    //释放锁
    public void unLock(){
        try {
            System.out.println(String.format("线程%s释放锁",threadName));
            zk.delete(pathName,-1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void process(WatchedEvent event) {
        // 如果第一个线程锁释放，其实只有第二个线程收到回调事件！！！

        // 如果不是 第一个，某一个挂了，也能造成后面的收到通知，从而让后面那个去watch挂掉这个前面的
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                zk.getChildren("/",false,this,"sdf");
                break;
            case NodeDataChanged:
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
    public void processResult(int rc, String path, Object ctx, String name) {
        System.out.println(String.format("rc:%s,path:%s,ctx:%s,String:%s",rc,path,ctx,name));
        if(name!=null){
            System.out.println(String.format("threadName:%s createNode %s",threadName,name));
            //查是不是目录中最小的
            this.pathName = name;
            zk.getChildren("/",false,this,"sdf");
        }
    }

    //getChildren call back
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children) {
        // 自己创建了，而且一定看到自己前面的节点
        /*System.out.println(String.format("%s locks...", threadName));
        for (String child : children) {
            System.out.println(child);
        }*/
        Collections.sort(children);
        int i = children.indexOf(pathName.substring(1));

        //是不是第一个
        if(i==0){
            //yes
            System.out.println(threadName+" i am first...");
            try {
                zk.setData("/",threadName.getBytes(),-1);//向锁目录设置数据
            } catch (KeeperException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            cdl.countDown();
        }else {
            //no
            zk.exists("/"+children.get(i-1),this,this,"sdf");
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        //todo
    }
}
