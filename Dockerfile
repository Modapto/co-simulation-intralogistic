FROM tomcat:8.5-jdk11-temurin

SHELL ["/bin/bash", "-c"]

ENV JAVA_OPTS="-Djdk.xml.xpathExprGrpLimit=0 -Djdk.xml.xpathExprOpLimit=0"
ENV TZ=Europe/Vienna
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN \
    dpkg --add-architecture i386 && \
    apt-get -q update && \
    apt-get -qy install maven && \
    apt-get -qy install nano && \
    apt-get -qy install wget && \
    apt-get -qy install unzip && \
    apt-get -qy install libc6:i386 libncurses5:i386 libstdc++6:i386 && \
    wget https://github.com/Modapto/co-simulation-intralogistic/archive/refs/heads/main.zip -O /opt/main.zip && \
    unzip /opt/main.zip -d /opt  && \
    rm /opt/main.zip && \
    mvn -B -f /opt/co-simulation-intralogistic-main/model-simulation-and-verification/pom.xml clean install && \
    unzip /opt/co-simulation-intralogistic-main/model-simulation-and-verification/target/model-simulation-verification.war -d /usr/local/tomcat/webapps/model-simulation-verification/  && \
    rm -r /opt/co-simulation-intralogistic-main && \
    apt-get -qy purge maven && \
    apt-get -qy autoremove && \
    rm -r /root/.m2/

EXPOSE 8080
CMD ["bash", "-c", "catalina.sh run"]
