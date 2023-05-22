package org.example;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class LeaderElection extends LeaderSelectorListenerAdapter {

    private final String name;
    private final LeaderSelector leaderSelector;
    private final NodeCache leaderCache;

    public LeaderElection(CuratorFramework client, String path, String name) {
        this.name = name;
        this.leaderSelector = new LeaderSelector(client, path, this);
        this.leaderSelector.autoRequeue();

        this.leaderCache = new NodeCache(client, "/newleaderelected");
        this.leaderCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                byte[] data = leaderCache.getCurrentData().getData();
                System.out.println("New leader elected: " + new String(data));
            }
        });
    }

    public void start() throws Exception {
        leaderSelector.start();
        leaderCache.start();
    }

    public void close() throws Exception {
        leaderSelector.close();
        leaderCache.close();
    }

    @Override
    public void takeLeadership(CuratorFramework client) throws Exception {
        System.out.println(name + " is the leader. Processing tasks...");
        client.setData().forPath("/newleaderelected", name.getBytes());
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            System.err.println(name + " was interrupted.");
            Thread.currentThread().interrupt();
        } finally {
            System.out.println(name + " relinquishing leadership.\n");
        }
    }

    public static void main(String[] args) throws Exception {
        String zkConnString = "192.168.1.105:2181";
        String path = "/leader_election";
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkConnString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        LeaderElection leader = new LeaderElection(client, path, args[0]);
        leader.start();
        System.out.println(args[0] + " has been registered for leader election. Waiting for leader election result...");
        Thread.sleep(Integer.MAX_VALUE);
    }
}