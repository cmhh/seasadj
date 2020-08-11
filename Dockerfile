FROM ubuntu:20.04

ENV DEBIAN_FRONTEND noninteractive
ENV SHELL /bin/bash
ENV SBT_VERSION 1.3.13

COPY src /seasadj/src
COPY project/assembly.sbt /seasadj/project/assembly.sbt
COPY project/build.properties /seasadj/project/build.properties
COPY build.sbt /seasadj/build.sbt

RUN  apt-get update && apt-get -y dist-upgrade && \
  apt-get install -y --no-install-recommends ca-certificates openjdk-8-jdk openjdk-8-jre wget && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* && \
  mkdir -p /tmp/x13 && \
  cd /tmp/x13 && \
  wget https://www.census.gov/ts/x13as/unix/x13ashtmlall_V1.1_B39.tar.gz && \
  tar -xvf x13ashtmlall_V1.1_B39.tar.gz && \
  mv x13ashtml /usr/bin/ && \
  cd / && \
  rm -fR /tmp/x13 && \
  wget https://piccolo.link/sbt-${SBT_VERSION}.tgz && \
  tar -xvf sbt-${SBT_VERSION}.tgz && \
  cd /seasadj && /sbt/bin/sbt assembly && cp target/scala-2.13/seasadj.jar  / && \
  apt-get remove -y openjdk-8-jdk wget && \ 
  apt-get autoremove -y && \
  apt-get clean && \
  cd / && \
  rm -fR /seasadj && \
  rm -fR /sbt* && \
  rm -fR /root/.ivy2 /root/.cache /root/.sbt

EXPOSE 9001

#CMD java -cp seasadj.jar org.cmhh.seasadj.Service && \
#  tail -f /dev/null

ENTRYPOINT ["java", "-cp", "/seasadj.jar", "org.cmhh.seasadj.Service"]