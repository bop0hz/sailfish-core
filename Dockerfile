FROM gradle:4.5.0-jdk8 AS builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean build zipRelease --no-daemon && \
    unzip ./FrontEnd/SailfishFrontEnd/build/release/sf_3.2.0_b0.zip && \
    unzip sfgui.war -d ./sfgui && \
    unzip ./BackEnd/Plugin/plugin-generic/build/release/generic/generic_3.2.0_b0_core_3.2.0.zip -d ./sfgui && \
    sed -i 's/<StorageType>db/<StorageType>file/' ./sfgui/cfg/sf.cfg.xml

FROM tomcat:9.0.26-jdk8-openjdk-slim
WORKDIR /usr/local/tomcat/webapps
COPY --from=builder --chown=1000 /home/gradle/src/sfgui ./sfgui
