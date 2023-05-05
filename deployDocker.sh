#!/bin/bash

apt install docker -y
echo '[Service]\nExecStart=\nExecStart=/usr/bin/dockerd' >> /etc/systemd/system/docker.service.d/override.conf
host_ip=$(ip -4 addr show docker0 | grep -Po 'inet \K[\d.]+')

echo '{"hosts": ["tcp://$host_ip:2375", "unix:///var/run/docker.sock"]}' >> /etc/docker/daemon.json
systemctl daemon-reload
systemctl restart docker.service
echo "Docker API will listen at $host_ip on port: 2375"

curl -L "https://github.com/docker/compose/releases/download/1.22.0/docker-compose-$(uname -s)-$(uname -m)" > ./docker-compose
sudo mv ./docker-compose /usr/bin/docker-compose
sudo chmod +x /usr/bin/docker-compose