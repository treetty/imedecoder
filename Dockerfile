FROM debian:jessie

#following block will install java, the official java docker is just using opensdk, many sdks can't be found
RUN \
    echo "===> add webupd8 repository..."  && \
    echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list  && \
    echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list  && \
    echo "deb http://ftp.de.debian.org/debian jessie main" >> /etc/apt/sources.list && \
    apt-key adv --keyserver keyserver.ubuntu.com --recv-keys EEA14886 && \
    apt-get update && \
    apt-get install -y gtk2-engines libxtst6 libxxf86vm1 freeglut3 libxslt1.1 && \
    apt-get update  && \
    \
    echo "===> install Java"  && \
    echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections  && \
    echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections  && \
    DEBIAN_FRONTEND=noninteractive  apt-get install -y --force-yes oracle-java8-installer oracle-java8-set-default  && \
    \
        apt-get install -y git && \
        cd ~ && git clone https://github.com/PatMartin/Dex.git && \
    \
    echo "===> clean up..."  && \
    rm -rf /var/cache/oracle-jdk8-installer && \
    apt-get clean  && \
    rm -rf /var/lib/apt/lists/*

#following is used to install unzip, which will be used when installing gradle
RUN apt-get update
RUN apt-get install -y python g++ make software-properties-common --force-yes
#RUN add-apt-repository ppa:chris-lea/node.js
#RUN apt-get update
# Install unzip
RUN apt-get install -y unzip


#grab gosu for easy step-down from root
ENV GOSU_VERSION 1.7
RUN set -x \
	&& wget -O /usr/local/bin/gosu "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$(dpkg --print-architecture)" \
	&& wget -O /usr/local/bin/gosu.asc "https://github.com/tianon/gosu/releases/download/$GOSU_VERSION/gosu-$(dpkg --print-architecture).asc" \
	&& export GNUPGHOME="$(mktemp -d)" \
	&& gpg --keyserver ha.pool.sks-keyservers.net --recv-keys B42F6819007F00F88E364FD4036A9C25BF357DD4 \
	&& gpg --batch --verify /usr/local/bin/gosu.asc /usr/local/bin/gosu \
	&& rm -r "$GNUPGHOME" /usr/local/bin/gosu.asc \
	&& chmod +x /usr/local/bin/gosu \
	&& gosu nobody true

#install gradle, first install curl
RUN apt-get update
RUN apt-get install -y curl

ENV GRADLE_VERSION 2.14.1

WORKDIR /usr/bin
RUN curl -sLO https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-all.zip && \
  unzip gradle-${GRADLE_VERSION}-all.zip && \
  ln -s gradle-${GRADLE_VERSION} gradle && \
  rm gradle-${GRADLE_VERSION}-all.zip

ENV GRADLE_HOME /usr/bin/gradle
ENV PATH $PATH:$GRADLE_HOME/bin

# add build files to container
ADD . /usr/local/imedecoder

WORKDIR /usr/local/imedecoder

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle/jre
#Setting the Default Java File Encoding to UTF-8,otherwise the encoding will have issue
ENV JAVA_TOOL_OPTIONS -Dfile.encoding=UTF8

RUN gradle build
CMD ["java", "-cp", "/usr/local/imedecoder/build/libs/imedecoder.jar:/usr/local/imedecoder/libs/boilerpipe-1.2.0.jar", "com.misingularity.cmdline.SougouExtractor", "pages.001", "pages.001.output"]

