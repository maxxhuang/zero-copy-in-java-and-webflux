# zero-copy-in-java-and-webflux

This application contains code examples to

1. demonstrate how to copy files using JDK API with and without zero-copy mechanism.
2. showcase several ways to serve static files in Spring WebFlux and measure the CPU usage.

It comes with no surprise that serving static files with zero-copy outperforms the one without zero-copy.

## Building executable jar

Spring Boot provides [spring-boot-maven-plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) that packages application classes along with all dependency jars into one single jar. Check out [here](https://docs.spring.io/spring-boot/docs/current/reference/html/executable-jar.html) for more details.

With the spring boot maven plugin specified in pom.xml, creating a Spring Boot executable jar is as simple as 

> ./mvnw clean package

which generates a repackaged jar (executable jar) under target folder (target/zerocopy-0.0.1-SNAPSHOT.jar).


## Building docker image

target/zerocopy-0.0.1-SNAPSHOT.jar needs to be available before building docker image. With the jar in place, run 

> docker build -t 10am/zerocopyapp .

Replace 10am/zerocopy with any tag name you prefer.

## Run the file server

For this file server to be able to report metrics, we need to bring up a graphite server. In this example, we'll use a predefined dockerized graphite server by [alex-mercer](https://hub.docker.com/r/alexmercer/graphite-grafana/~/dockerfile/):

> docker run \\\
-p 2003:2003 \\\
-p 3000:3000 \\\
-p 8888:80 \\\
-d \\\
alexmercer/graphite-grafana

With the graphite server up and running, we can proceed to start the file server:

>docker run \\\
-e MONITOR_INTERVAL=[metric reporting interval in seconds] \\\
-e GRAPHITE_IP=[graphite server ip] \\\
-e GRAPHITE_PORT=[graphite server port] \\\
-p 8080:8080 \\\
10am/zerocopyapp

Again, 10am/zerocopyapp should be replaced with your own choice.

Among the environmental variables above, GRAPHITE_IP is mandatory while GRAPHITE_PORT (defaults to 2003) and MONITOR_INTERVAL (defaults to 10) are optional and you can safely skip them.

Run the command to keep the file server busy.

### zero copy

> while true; do curl -v [file server ip]:8080/zerocopy1 > /dev/null; done

### non zero copy

> while true; do curl -v [file server ip]:8080/nonzerocopy > /dev/null; done

## Observing CPU metrics

While the file server is working to serve files, go to http://[graphite server ip]:8888 to observe the metrics in graphite web.

Or if you prefer a fancy dashboard and you happen to have a grafana server brought up just as the one we gave above (alexmercer/graphite-grafana), go to http://[grafana server ip]:3000 to create your own modern metrics dashboard.
