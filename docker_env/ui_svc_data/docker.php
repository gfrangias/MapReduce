<?php

require 'vendor/autoload.php';

use Docker\Docker;

$docker = new Docker();
$containers = $docker->getContainerManager()->findAll();

print_r($containers);