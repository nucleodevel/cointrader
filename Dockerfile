FROM openjdk:11-jre

RUN mkdir /usr/share/cointrader
RUN mkdir /etc/cointrader

RUN touch /var/log/cointrader.log
RUN ln -sf /dev/stdout /var/log/cointrader.log

COPY target/cointrader.jar /usr/share/cointrader/cointrader.jar
COPY src/main/resources/docker/scripts/run /usr/share/cointrader/run

RUN chmod +x /usr/share/cointrader/run
CMD ["/usr/share/cointrader/run"]