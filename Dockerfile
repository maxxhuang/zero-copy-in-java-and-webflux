FROM openjdk:8

USER root

RUN apt-get -y update
RUN apt-get install -y sysstat
RUN apt-get install -y collectd

RUN useradd -m -g root collector
RUN echo 'export APP_NAME=`cat /appname`' >> /home/collector/.profile

RUN mkdir -p /var/log/collectd

#install sudo package and add the user “collector” to sudoers
RUN printf 'N\n' | apt-get install -y sudo
RUN echo 'collector   ALL=(ALL)       NOPASSWD: ALL' >> /etc/sudoers

COPY src/main/docker/collectd/collectd.conf /etc/collectd/
COPY src/main/docker/collectd/write_log.conf /etc/collectd.d/
COPY src/main/docker/collectd/write_graphite.conf /etc/collectd.d/
COPY src/main/docker/collectd/exec.conf /etc/collectd.d/

COPY src/main/docker/collectd/monitor-script /monitor-script/
RUN chmod a+x /monitor-script/monitor_ps.sh

COPY src/main/docker/set_variable.sh /
COPY src/main/docker/echo_variable.sh /
COPY src/main/docker/set_graphite_conf.sh /
COPY src/main/docker/entrypoint.sh /

COPY src/main/docker/scalaz.pdf /public/scalaz.pdf

COPY target/zerocopy-0.0.1-SNAPSHOT.jar /zerocopy.jar


ENV APP_NAME=zerocopy
ENV GRAPHITE_IP=127.0.0.1
ENV GRAPHITE_PORT=2003
ENV MONITOR_INTERVAL=10

ENV DOWNLOAD_FILE=/public/scalaz.pdf

EXPOSE 8080

RUN chmod u+x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

CMD ["web"]