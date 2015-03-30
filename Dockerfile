FROM java:8
RUN apt-get update 
RUN apt-get install -qy dnsutils
RUN mkdir -p /opt/ctrl-pkw
ADD build/distributions/ctrl-pkw.tgz /opt/
WORKDIR /opt/ctrl-pkw
CMD bin/ctrl-pkw --cassandra.contactPoint=`dig +short $CASSANDRA_CONTACT_POINT | head -n1` --cassandra.port=9042
