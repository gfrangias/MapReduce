package org.example;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import java.util.Arrays;

public class DockerController {

    public boolean deployMonitor(String containerName, String dedicatedTo) {
        DefaultDockerClientConfig.Builder configBuilder = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://172.17.0.1:2375")
                .withDockerTlsVerify(false);

        DockerClient dockerClient = DockerClientBuilder
                .getInstance(configBuilder.build())
                .build();
        String imageName = "zoopsworker";
        String volumePathOnHost = "/home/user/mp/MapReduce/docker_env/filedb/uploads";
        String volumePathInContainer = "/app/uploads";
        String networkName = "map_reduce_net";

        // Set volume
        Volume volume = new Volume(volumePathInContainer);
        Bind bind = new Bind(volumePathOnHost, volume);
        HostConfig hostConfig = new HostConfig().withBinds(bind);

        // Set environment variables
        String[] environment = new String[]{
                "CONTAINER_NAME=" + containerName,
                "DEDICATED_TO=" + dedicatedTo
        };

        // Create container
        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                .withHostConfig(hostConfig)
                .withEnv(Arrays.asList(environment))
                .withCmd("command", "params")  // optional, replace with your command
                .exec();

        // Set network
        dockerClient.connectToNetworkCmd()
                .withContainerId(container.getId())
                .withNetworkId(networkName)
                .exec();

        // Start container
        dockerClient.startContainerCmd(container.getId()).exec();
        return true;
    }
}
