FROM gradle:4.5.0-jdk8 AS builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean build publishPlugin zipRelease --no-daemon && \
    unzip ./FrontEnd/SailfishFrontEnd/build/release/sf_3.2.0_b0.zip && \
    unzip ./BackEnd/Plugin/plugin-generic/build/release/generic/generic_3.2.0_b0_core_3.2.0.zip

FROM tomcat:9.0.26-jdk8-openjdk-slim
WORKDIR /usr/local/tomcat/webapps
COPY --from=builder /home/gradle/src/sfgui.war .
RUN jar xvf sfgui.war . && \
    rm sfgui.war && \
    sed -i 's/\<StorageType\>db/\<StorageType\>file/g' ./sfgui/cfg/sf.cfg.xml

WORKDIR /usr/local/tomcat/webapps/sfgui/plugins/
COPY --from=builder /home/gradle/src/plugins/ .
WORKDIR /usr/local/tomcat/webapps/sfgui/
RUN sed -i 's/<StorageType>db/<StorageType>file/' ./cfg/sf.cfg.xml
