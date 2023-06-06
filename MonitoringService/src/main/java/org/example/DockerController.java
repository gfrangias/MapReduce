package org.example;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;

import java.util.List;

public class DockerController {

    private final DockerClient dockerClient;

    public DockerController() {
        dockerClient = DockerClientBuilder.getInstance().build();
    }

    public void deployContainer(String imageName, String hostDirectory, String containerDirectory, String networkMode) {
        // Define the volume
        Volume volume1 = new Volume("/app/uploads");

        // Bind the volumes
        Bind bind1 = new Bind("/host/volume/path1", volume1);

        // Create container
        CreateContainerResponse container = dockerClient.createContainerCmd("docker_env_fathermonitor")
                .withVolumes(volume1)
                .withBinds(bind1)
                .withName("your_container")
                .withNetworkMode("your_network")
                .exec();

        // Start the container
        dockerClient.startContainerCmd(container.getId()).exec();
    }


}
