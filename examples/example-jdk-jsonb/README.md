# example-jdk-jsonb

- Minimal example of Jex web routing with underlying JDK Http server
- Uses avaje-jsonb for json marshalling (via source code generation)

## Graalvm native-image

To Build
```sh
mvn clean package -Pnative -DskipTests
```

To Run:
```sh
./target/mytest

# produces
# Oct 21, 2022 1:39:36 PM io.avaje.jex.core.JdkServerStart start
# INFO: started server on port 7003 version 2.5-SNAPSHOT
```

To Play:
```sh
curl localhost:7003/foo/44

# produces
# {"id":44,"name":"Rob"}
```
