FROM ghcr.io/graalvm/graalvm-ce:java11-21.2.0

RUN mkdir -p /tmp/x13 && \
  cd /tmp/x13 && \
  curl -s https://www2.census.gov/software/x-13arima-seats/x13as/unix-linux/program-archives/x13as_html-v1-1-b58.tar.gz | tar -xzvf - && \
  mv x13as/x13as_html /usr/bin/x13ashtml && \
  cd / && \
  rm -fR /tmp/x13

COPY target/scala-2.13/seasadj.jar seasadj.jar

EXPOSE 9001

ENTRYPOINT ["java", "-cp", "/seasadj.jar", "org.cmhh.seasadj.Service"]
