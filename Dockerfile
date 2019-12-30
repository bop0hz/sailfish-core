FROM gradle:4.5.0-jdk8 AS builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean build publishPlugin zipRelease --no-daemon

FROM tomcat:9.0.26-jdk8-openjdk-slim
WORKDIR /usr/local/tomcat/webapps
COPY --from=builder /home/gradle/src/FrontEnd/SailfishFrontEnd/build/release/sf_3.2.0_b0.zip .
RUN unzip sf_3.2.0_b0.zip && \
    jar xvf sfgui.war . && \
    rm sf_3.2.0_b0.zip sfgui.war && \
    sed -i 's/\<StorageType\>db/\<StorageType\>file/g' ./sfgui/cfg/sf.cfg.xml

WORKDIR /usr/local/tomcat/webapps/sfgui/plugins/
COPY --from=builder /home/gradle/src/BackEnd/Plugin/plugin-generic/build/release/generic/generic_3.2.0_b0_core_3.2.0.zip .
RUN unzip generic_3.2.0_b0_core_3.2.0.zip && \
    rm generic_3.2.0_b0_core_3.2.0.zip && \
    sed -i 's/\<StorageType\>db/\<StorageType\>file/g' ./sfgui/cfg/sf.cfg.xml
