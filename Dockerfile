FROM openjdk:11-jre

RUN mkdir /usr/share/cointrader
RUN mkdir /etc/cointrader
RUN mkdir /var/log/cointrader

RUN touch /var/log/cointrader/cointrader.log
RUN ln -sf /dev/stdout /var/log/cointrader/cointrader.log

COPY target/cointrader.jar /usr/share/cointrader/cointrader.jar
COPY src/main/resources/docker/entrypoint/run /usr/share/cointrader/run

RUN chmod +x /usr/share/cointrader/run
CMD ["/usr/share/cointrader/run"]