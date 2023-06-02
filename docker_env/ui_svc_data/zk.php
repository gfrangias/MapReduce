<?php

// Array of ZooKeeper addresses
$zookeeperAddresses = [
    '172.16.0.11:2181',
    '172.16.0.12:2181',
    '172.16.0.13:2181'
];

// Select a random ZooKeeper address
$randomAddress = $zookeeperAddresses[array_rand($zookeeperAddresses)];

// Connect to ZooKeeper
$zk = new Zookeeper($randomAddress);