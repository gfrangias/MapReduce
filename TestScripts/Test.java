
import java.util.*;
import java.io.*;

public class Test {
    public static void main(String[] args) throws Exception {
        Process p = new ProcessBuilder("cd ../docker_env/filedb/uploads/admin/executables && java -cp exec.jar Mapper").start();

        int code = p.waitFor();

        System.out.println(code);

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        reader.close();
    }
}