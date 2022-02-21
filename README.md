# Distributed Systems

This project is intended for building a scalable distributed cloud-based system that can record all lift rides from all Upic resorts. This data can then be used as a basis for data analysis for Upic - a global acquirer of ski resorts that is homogenezing skiing around the world.


## Server API
The API endpoints and model schemas follow the specifications defined in Swagger. [](https://app.swaggerhub.com/apis/cloud-perf/SkiDataAPI/1.16)

API diagram

The server is implemented by Java servlets. JSON object is used for data transmission. Each servlet API is tested with POSTMAN.
UML diagram


The resulting .war file has been deployed to Tomcat folder under a running AWS EC2 instance.


## Client
This is a multithreaded Java client that can be configured to upload a day of lift rides to the server and exert various loads on the server. 
The client accept a set of parameters from the command line (or a parameter file) at startup. 
The client will execute 3 phases, with each phase sending a large number of lift ride events to the server API.

* start up - # of threads: numThreads/4. Once 20% of the threads in Phase 1 have completed, Phase 2 starts.
* peak - # of threads: numThreads. Once 20% of the threads in Phase 2 have completed, Phase 3 starts.
* cool down - # of threads: numThreads/10.

UML

CountDownLatch is used control when phase 2 and 3 starts. It is constructed by passing in how many threads are completed before releasing and proceeding into next phase. 

CountDownLatch is also used at the end of 3 phases. Here I find sometimes it takes more time to call Stats object to increase or decrease # of successful or failed requests, than the API calls. This triggers the problem when printing out the results. The count of successful & failed requests is much lower than the total number of requests. The print stats method happens earlier, and the synchronized methods haven’t completed yet. To solve it, each phase also has a CountDownLatch with their total number of requests. This makes sure all the synchronized methods are completed before generating and printing out the statistic information.

## Throughput
