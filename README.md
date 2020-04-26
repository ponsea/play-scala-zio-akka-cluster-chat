# How to run in local
1. Start DB Server (MySQL)
    ```
    $ cd docker
    $ docker-compose up -d
    ```
2. Start Application Servers
    - Server 1 (http => localhost:9000)
    ```
    $ sbt interfaces/run
    ```
    - Server 2 (http => localhost:9001)
    ```
    $ sbt "interfaces/run -Dhttp.port=9001 -Dconfig.resource=application-2.conf"
    ```
  
3. Access to `http://localhost:9000/ (or 9001)`
