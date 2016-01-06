name := """higouAPI"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
    javaWs,
    javaCore,
    javaJpa.exclude("com.jolbox", "bonecp").exclude("org.hibernate.javax.persistence", "hibernate-jpa-2.0-api"),
    cache,
    "org.springframework" % "spring-context" % "4.1.4.RELEASE",
    "javax.inject" % "javax.inject" % "1",
    "org.springframework.data" % "spring-data-jpa" % "1.7.2.RELEASE",
    "org.springframework" % "spring-expression" % "4.1.4.RELEASE",
    "org.springframework" % "spring-context-support" % "4.1.4.RELEASE",
    "org.hibernate" % "hibernate-entitymanager" % "4.3.8.Final",
    "org.mockito" % "mockito-core" % "1.9.5" % "test",
    "mysql" % "mysql-connector-java" % "5.1.18",
    "org.apache.poi" % "poi" % "3.10-FINAL",
    "org.apache.poi" % "poi-ooxml" % "3.10-FINAL",
    "javax.mail" % "mail" % "1.4.7",
    "commons-collections" % "commons-collections" % "3.2.1",
    "org.apache.commons" % "commons-lang3" % "3.3.1",
    "com.github.mumoshu" %% "play2-memcached" % "0.6.0",
    "org.apache.httpcomponents" % "httpclient" % "4.3.6",
    "org.apache.httpcomponents" % "httpmime" % "4.3.6",
    "commons-httpclient" % "commons-httpclient" % "3.1",
    "jaxen" % "jaxen" % "1.1.1",
    "net.logstash.logback" % "logstash-logback-encoder" % "3.6",
    "ch.qos.logback" % "logback-core" % "1.1.2",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "log4j" % "log4j" % "1.2.17",
    "com.alibaba" % "druid" % "1.0.12",
    "com.google.code.gson" % "gson" % "2.2.4",
    "jdom" % "jdom" % "1.1",
    "org.json" % "json" % "20080701",
    "com.thoughtworks.xstream" % "xstream" % "1.4.7",
    "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.4",
    "com.google.protobuf" % "protobuf-java" % "2.5.0",
    "com.caucho" % "hessian" % "4.0.7",
    "com.rabbitmq" % "amqp-client" % "3.5.1",
    "net.lingala.zip4j" % "zip4j" % "1.3.2",
    "org.springframework.amqp" % "spring-amqp" % "1.1.1.RELEASE",
    "org.springframework.amqp" % "spring-rabbit" % "1.1.1.RELEASE",
    "org.sedis"                 %%  "sedis"             % "1.2.2"
)

