# In-Memory Key/Value Data Server
 
This is a Spring Boot application that services as an in-memory key/value data store server. The application supports basic read write delete of key values. It also supports a sequence of multiple requests to be executed as part of a transaction. Spring Boot uses a thread pool to handle multiple incoming requests and leverages multithreading capabilities of the underlying Tomcat servlet container. 

### Software Quality Considerations

Spring Boot was chosen for its ease of setup from spring.io with a large and active community provides ample resources, tutorials, and support. Listed below are software quality aspects that this application supports. 

* Scalability  

Using SpringBoot enables horizontal scaling by adding more instances with a load balancer of the application to distribute the workload.  

* Reliability

Spring Boot is built upon the mature Spring Framework with years of development and community support. It has been widely used in enterprise applications with proven reliability.


* Maintainability   

Spring Boot is a widely used framework with an abundance of examples and troubleshooting support. 

* Portability  

The Spring framework allows external configuration through properties and yaml files. It is easy to tailor the application's behavior to different environments without modifying the code. Spring Boot applications can be easily containerized using Docker.  

* Testability

Spring Boot allows testing specific parts of the application, such as controllers, services, or repositories, in isolation by using the Mockito testing framework provided in spring-boot-starter-test dependency.

* Replaceability

Separate controller, services, and repositories components allow easy replacement of individual components without the need to change other components. There are two repository implementations, one using RDBMS and JDBC transactions and another using custom transactions with in memory map data storage. With the RepositoryConfig specification, the selection of the implementing class is defined in the application.properties. 

### Software Design  

The following diagram describes the Spring application components and their interactions. The separation of controller, service, repositories reinforces the principal of single responsibility where each component performs one task.  

![Alt text](Design.jpg?raw=true "Architectural Design")

#####  KeyValueStoreController

The KeyValueStoreController defines the API end points for each request. For the composite request, the controller defines a list of KeyValueStoreRequest to be passed to the service layer. 

#####  KeyValueStoreService

The KeyValueStoreService processes each request from the controller. Any data and business validations are handled in this layer. 

#####  KeyValueRepository

The KeyValueRepository interface defines operations to the keyvalue store. There are two implementations of the repository, one uses an RDBMS database and the other uses an in-memory hash map. The RDBMSKeyValueRepository is responsible for database calls and transaction handling using JDBC. The MemStoreKeyValueRepository uses its own transaction handling. For each request, the repository creates a KeyValueStoreResponse object which contains the status of the request and any errors that may have occurred. 

### Database

The H2 in memory database is used for the RDBMSKeyValueRepository implementation. The KeyValueEntity.java class is declared as a JPA @Entity with @Column search key and search value. When the Spring application starts, it will create the keyvaluepairs table based on the @Entity and  @Column annotations.   

Once the Spring application is running, the h2 database console will be available at /h2-console to review the database and to execute sql statements directly. 

![Alt text](H2console.jpg?raw=true "H2 Console")

For the MemStoreKeyValueRepository, an in-memory datamap is used and custom transaction handling is applied. 

##### Database Access and Transactional Support  

* RDBMSKeyValueRepository  

The Java JDBC framework will be used to manage transactions to the database for the RDBMSKeyValueRepository implementation. By default, the JDBC connection has the auto-commit flag set to true which allows each request to be committed automatically at the end of the execution. For processing multiple requests in the same transaction, the connection's auto-commit flag needs to be set to false so that the entire set of requests can be rolled back together if there are any errors. 

For the search request, if a search key is not found, an error is raised. If search is part of a multi-request that includes save or delete actions, the save or delete will be rolled back. 

* MemStoreKeyValueRepository  

Custom transaction handling is used for the MemStoreKeyValueRepository implementation. When multiple requests are received, a transaction object is created for each request in the order it is received. To commit the transactions, a minimum heap structure is used to store the transactions where the sequence number is used to determine the heap order. The commitment is executed in order. For rollback, a max heap is used to order the transactions in reverse order and the last committed transaction is rolled back first. 

##### Repository Selection Tradeoffs
| Selection| Maintainability  | Scalability |
| :---:   | :---: | :---: | 
| in-memory | easier to understand   | can only provide limited vertical scaling|
| DBMS | Using JDBC transaction management is easier and more robust than custom built transaction | Horizontal scaling is possible with different DB cluster deployment stragegies such as sharding or replication |
    

### Sample Request and Responses

##### Save a name/value pair to the store
PUT: localhost:9888/api/keyvaluestore/winstonC  
Payload: {"first_name": "Winston", "last_name": "Churchill", "role": "Prime Minister"}  
Response: {"status":"OK"}  

    curl --location --request PUT 'localhost:9888/api/keyvaluestore/winstonC' \
    --header 'Content-Type: text/plain' \
    --data '{"first_name": "Winston", "last_name": "Churchill", "role": "Prime Minister"}'

##### Get the value of a key from the store
GET: localhost:9888/api/keyvaluestore/winstonC  
Response:  
{  
    "status": "OK",  
    "result": "{"first_name": "Winston", "last_name": "Churchill", "role": "Prime Minister"}"  
}  

When the key is not found:
Response: 
{
    "status": "Error",
    "mesg": "Key not found"
}

    curl --location 'localhost:9888/api/keyvaluestore/winstonC' \
    --header 'Content-Type: application/json' \
    --data ''

##### Delete the value of a key from the store 
DELETE: localhost:9888/api/keyvaluestore/winstonC  
Response: {"status":"OK"}  

When the key is not found:
Response: 
{
    "status": "Error",
    "mesg": "Key not found"
}

    curl --location --request DELETE 'localhost:9888/api/keyvaluestore/winstonC'

##### Composite Request  

The compoiste request allows multiple requests to be processed at once within the same transaction. The entire request fails if any one action failed. The request body is a JSON array where each request item has the key, action, and an optional payload for save.  

    curl --location 'localhost:9888/api/keyvaluestore/composite' \
    --header 'Content-Type: application/json' \
    --data '[
    {
        "key": "georgew",
        "action": "Save",
        "payload": {
            "first_name": "George",
            "last_name": "Washington",
            "role": "President"
        }
    },
    {
        "key": "georgew",
        "action": "Search"
    },
    {
        "key": "georgew",
        "action": "Delete"
    }
    ]'

The response contains the result of each request along with the original request and payload. 

    PUT georgew {"first_name":"George","last_name":"Washington","role":"President"}
    {"status":"OK"}
    GET georgew
    {"status":"OK","result":"{"first_name":"George","last_name":"Washington","role":"President"}"}
    DELETE georgew
    {"status":"OK"}

### Docker  

The dockerfile exposes port 9888.

  * docker build -t spring/keyvalue-spring-boot-docker .
  * docker run -p 9888:8080 spring/keyvalue-spring-boot-docker



 


