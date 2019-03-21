FROM neo4j

ENV APOC_VERSION 3.5.0.1
ENV APOC_URI https://github.com/neo4j-contrib/neo4j-apoc-procedures/releases/download/${APOC_VERSION}/apoc-${APOC_VERSION}-all.jar

RUN wget $APOC_URI && mv apoc-${APOC_VERSION}-all.jar plugins/apoc-${APOC_VERSION}-all.jar

COPY neo4j.conf /var/lib/neo4j/conf/

EXPOSE 7474 7473 7687

CMD ["neo4j"]