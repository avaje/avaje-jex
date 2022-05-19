import boot.BootJex;

public class Main {

  public static void main(String[] args) throws InterruptedException {
    BootJex.start();

    Thread.sleep(5000);
    BootJex.stop();

    Thread.sleep(5000);
    BootJex.restart();
  }

}
