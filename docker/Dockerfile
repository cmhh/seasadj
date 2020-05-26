FROM oracle/graalvm-ce:latest

RUN yum install -y curl && \
  mkdir -p /tmp/x13 && \
  cd /tmp/x13 && \
  curl -s https://www.census.gov/ts/x13as/unix/x13ashtmlall_V1.1_B39.tar.gz | tar -xvf - && \
  mv x13ashtml /usr/bin/ && \
  cd .. && \
  rm -fR /tmp/x13

ADD seasadj.jar seasadj.jar

EXPOSE 9001

CMD java -cp seasadj.jar org.cmhh.seasadj.Service
