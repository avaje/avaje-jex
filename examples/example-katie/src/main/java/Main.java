import io.avaje.inject.BeanScope;
import io.avaje.jex.Jex;

public class Main {

  public static void main(String[] args) {

    BeanScope beanScope = BeanScope.builder()
      .build();

    Jex jex = beanScope.getOptional(Jex.class)
      .orElse(Jex.create());
    jex.configureWith(beanScope)
    //.routing(routes)
    //.port(8001)
    ;


    jex.lifecycle().onShutdown(beanScope::close);

    Jex.Server server = jex.start();

  }

}
