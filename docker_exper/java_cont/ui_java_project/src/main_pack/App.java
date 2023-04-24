package src.main_pack;

public class App {
    public App() {
    }

    public static void main(String[] args) throws InterruptedException {
        App app = new App();
        System.out.println("Hello World!");
        synchronized (app) {
            app.wait();
        }
        System.out.println("App terminated");
    }
}
