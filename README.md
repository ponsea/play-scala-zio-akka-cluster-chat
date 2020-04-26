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
