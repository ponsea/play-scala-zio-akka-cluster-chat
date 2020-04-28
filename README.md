# Documentation
- [クラスタリングされたリアルタイムなチャットサーバーをクリーンアーキテクチャ風に作るサンプル その1 設計 (全2篇) | Scala, Playframework, Akka, ZIO - Web開発のしおりRepository](https://ponsea.hatenablog.com/entry/2020/04/28/223728)

# How to run in local
1. Start DB Server (MySQL)
    ```
    $ cd docker
    $ docker-compose up -d
    ```
   MySQL server will start and `sample` database will be created.
   
   If you want to access the DB
   ```
   $ mysql -h 127.0.0.1 -usample -p # the password is `sample`
   ```
2. Apply table schemas in the DB
    ``` 
    $ sbt flyway-sample/flywayMigrate
    ```
3. Start Application Servers
    - Server 1 (http => localhost:9000)
    ```
    $ sbt interfaces/run
    ```
    - Server 2 (http => localhost:9001)
    ```
    $ sbt "interfaces/run -Dhttp.port=9001 -Dconfig.resource=application-2.conf"
    ```
  
4. Access to `http://localhost:9000/ (or 9001)`

# Network layout in production
![Cluster Chat Architecture (1)](https://user-images.githubusercontent.com/19386206/80488739-ca953880-8999-11ea-9cce-97e387ad3344.png)

# Relation of modules and classes
![Cluster Chat Classes Architecture (1)](https://user-images.githubusercontent.com/19386206/80489015-324b8380-899a-11ea-8105-3b3133685ccc.png)

# Demo
![play-scala-zio-akka-cluster-chat-demo (1)](https://user-images.githubusercontent.com/19386206/80489215-76d71f00-899a-11ea-8324-b9f4eee03319.gif)