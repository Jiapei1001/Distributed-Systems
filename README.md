# Scalable Distributed Systems

This project is intended for building a scalable distributed cloud-based system that can record all lift rides from all Upic resorts. This data can then be used as a basis for data analysis for Upic - a global acquirer of ski resorts that is homogenezing skiing around the world.

![profile_edit](https://user-images.githubusercontent.com/20607583/154911354-053248ef-2142-466d-a763-d7c2d61fabd4.jpeg)


## Restful API
The API endpoints and model schemas are implemented by client's specifications . [](https://app.swaggerhub.com/apis/cloud-perf/SkiDataAPI/1.16)
<p align="center">
<img width="600" alt="Swagger APIs" src="https://user-images.githubusercontent.com/20607583/154888454-ae0dc003-602d-4e20-b6b0-80c48ce2a888.png">
</p>


## Message Processing

![A2_server_diagram](https://user-images.githubusercontent.com/20607583/158247392-8271d8b2-d236-4a67-b65e-243c13d10904.png)


## Server
The server is implemented by Java servlets. JSON object is used for data transmission. Each servlet API is tested with POSTMAN.
![uml](https://user-images.githubusercontent.com/20607583/154888583-67a1417c-f4d4-4126-b062-c8b101268c24.png)



The resulting .war file has been deployed to Tomcat folder under a running AWS EC2 instance.


## Client
This is a multithreaded Java client that can be configured to upload a day of lift rides to the server and exert various loads on the server. 
The client accept a set of parameters from the command line (or a parameter file) at startup. 

The client will execute 3 phases, with each phase sending a large number of lift ride events to the server API.

* __Start Up phase__ - # of threads: numThreads/4. Once 20% of the threads in Phase 1 have completed, Phase 2 starts.
* __Peak phase__ - # of threads: numThreads. Once 20% of the threads in Phase 2 have completed, Phase 3 starts.
* __Cool Down phase__ - # of threads: numThreads/10.

![uml](https://user-images.githubusercontent.com/20607583/154888904-46444245-3d4e-40b4-82ae-6ec6dbb0b931.png)


CountDownLatch is used control when phase 2 and 3 starts. It is constructed by passing in how many threads are completed before releasing and proceeding into next phase. 

CountDownLatch is also used at the end of 3 phases. Here I find sometimes it takes more time to call Stats object to increase or decrease # of successful or failed requests, than the API calls. This triggers the problem when printing out the results. The count of successful & failed requests is much lower than the total number of requests. The print stats method happens earlier, and the synchronized methods haven’t completed yet. To solve it, each phase also has a CountDownLatch with their total number of requests. This makes sure all the synchronized methods are completed before generating and printing out the statistic information.

## Throughput & Latency

The client is ran with __32, 64, 128__ and __256 threads__, with numSkiers=20000, and numLifts=40. 

A single request is used to estimate this latency. Follow __Little's Law__, the resulting throughput are calculated and compared.

Key indexes:

- __mean__ response time (millisecs)
- __median__ response time (millisecs)
- __throughput__, total number of requests/wall time (requests/second)
- __p99__ (99th percentile) response time
- __min__ and max response time (millisecs)


<p align="center">
<img width="600" alt="p2_128" src="https://user-images.githubusercontent.com/20607583/154889902-51cc5005-276b-439b-b801-993be23270b8.png">
</p>

<p align="center">
<img width="600" alt="p2_128" src="https://user-images.githubusercontent.com/20607583/154890168-d79603f8-1200-4a18-a13e-b812bb5332a5.png">
</p>

