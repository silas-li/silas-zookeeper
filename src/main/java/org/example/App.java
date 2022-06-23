package org.example;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, InterruptedException, KeeperException {

        System.out.println( "Hello World!" );

        //zk是有session概念的，没有连接池的概念
        //watch观察，回调
        //watch的注册只会发生在读类型调用，比如get exist...
        //第一类，new ZK的时候传入的watch,这个是session级别的，跟path/node没有关系

        //异步的同步问题
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        //sessionTimeout链接断开之后 临时节点保存时间
        final ZooKeeper zk = new ZooKeeper("10.255.72.158:2181,10.255.72.211:2181,10.255.72.212:2181,10.255.72.213:2181", 3000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                Watcher.Event.KeeperState state = watchedEvent.getState();
                Watcher.Event.EventType type = watchedEvent.getType();
                String path = watchedEvent.getPath();
                System.out.println("new zk watch "+watchedEvent.toString());
                switch (state) {
                    case Unknown:
                        break;
                    case Disconnected:
                        break;
                    case NoSyncConnected:
                        break;
                    case SyncConnected:
                        System.out.println("SyncConnected");
                        countDownLatch.countDown();
                        break;
                    case AuthFailed:
                        break;
                    case ConnectedReadOnly:
                        break;
                    case SaslAuthenticated:
                        break;
                    case Expired:
                        break;
                    case Closed:
                        break;
                }

                switch (type) {
                    case None:
                        break;
                    case NodeCreated:
                        break;
                    case NodeDeleted:
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
        });

        //阻塞
        countDownLatch.await();
        ZooKeeper.States states = zk.getState();
        switch (states) {
            case CONNECTING:
                System.out.println("CONNECTING");
                break;
            case ASSOCIATING:
                break;
            case CONNECTED:
                System.out.println("CONNECTED");
                break;
            case CONNECTEDREADONLY:
                break;
            case CLOSED:
                break;
            case AUTH_FAILED:
                break;
            case NOT_CONNECTED:
                break;
        }

        //同步的 没有回调的
        String path = zk.create("/a","olddata".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

        final Stat stat = new Stat();
        byte[] node = null;
        /*
        // boolean watch false只取数据
        node = zk.getData("/a",false,stat);
        System.out.println(new String(node));
        System.out.println(stat);
        */
        node = zk.getData("/a", new Watcher() {
            // watch,path上的watch是一次性的
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println("zk.getData patth:/a 回调 "+watchedEvent.toString());
                //重复注册
                try {
                    //boolean watch 为true的时候 调用的是 new zk的时候 default的watch被注册
                    //zk.getData("/a",true,stat);
                    //this  ,此处原来的匿名watch
                    zk.getData("/a",this,stat);
                } catch (KeeperException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, stat);
        System.out.println(new String(node));
        System.out.println(stat);
        //触发回调
        Stat stat1 =zk.setData("/a","newdata1".getBytes(),0);
        //还会触发回调吗（什么都不做的话 就不会了 除非在原来的watch重复注册）
        Stat stat2 =zk.setData("/a","newdata2".getBytes(),stat1.getVersion());


        //异步的 非阻塞的方式
        System.out.println("------------async start------------");
        zk.getData("/a", false, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                System.out.println("------------async callBack------------");
                System.out.println(ctx.toString());
                System.out.println(new String(data));
            }
        },"abc");
        System.out.println("------------async end------------");

        Thread.sleep(10000);
    }
}
