FROM java:8
RUN mkdir -p /opt/ctrl-pkw
ADD build/distributions/ctrl-pkw.tgz /opt/
WORKDIR /opt/ctrl-pkw
CMD bin/ctrl-pkw
