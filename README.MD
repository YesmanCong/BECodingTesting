# ping-server

运行多个实例
``` shell
java -jar ping-server-0.0.1-SNAPSHOT.jar --server.port=8081
java -jar ping-server-0.0.1-SNAPSHOT.jar --server.port=8082
java -jar ping-server-0.0.1-SNAPSHOT.jar --server.port=8083
```


# pong-server

运行多个实例

``` shell
java -jar pong-server-0.0.1-SNAPSHOT.jar --server.port=8070
```
# jar 文件

[ping-server-0.0.1-SNAPSHOT.jar](./ping-server-0.0.1-SNAPSHOT.jar)

[pong-server-0.0.1-SNAPSHOT.jar](./pong-server-0.0.1-SNAPSHOT.jar)


# 测试结果位置

## ping-server
[target/site/jacoco-ut/index.html](./ping-server/target/site/jacoco-ut/index.html)

[target/jacoco.exec](./ping-server/target/jacoco.exec)

## pong-server
[target/site/jacoco-ut/index.html](./pong-server/target/site/jacoco-ut/index.html)

[target/jacoco.exec](./pong-server/target/jacoco.exec)