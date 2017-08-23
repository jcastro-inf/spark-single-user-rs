#!/bin/bash

INSPECTIT_AGENT_HOME="/home/jcastro/inspectIT/agent"
REPOSITORY_IP="127.0.0.1"
REPOSITORY_PORT="9070"
AGENT_DISPLAY_NAME="spark-single-user-rs"

java -Dspark.master=local[*] \
		-javaagent:$INSPECTIT_AGENT_HOME/inspectit-agent.jar  \
		-Dinspectit.repository=$REPOSITORY_IP:$REPOSITORY_PORT -Dinspectit.agent.name=$AGENT_DISPLAY_NAME \
		-classpath target/original-spark-single-user-rs-1.0.jar:/usr/lib/jvm/java-8-oracle/jre/lib/charsets.jar:/usr/lib/jvm/java-8-oracle/jre/lib/deploy.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/cldrdata.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/dnsns.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/jaccess.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/jfxrt.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/localedata.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/nashorn.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/sunec.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/sunjce_provider.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/sunpkcs11.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/zipfs.jar:/usr/lib/jvm/java-8-oracle/jre/lib/javaws.jar:/usr/lib/jvm/java-8-oracle/jre/lib/jce.jar:/usr/lib/jvm/java-8-oracle/jre/lib/jfr.jar:/usr/lib/jvm/java-8-oracle/jre/lib/jfxswt.jar:/usr/lib/jvm/java-8-oracle/jre/lib/jsse.jar:/usr/lib/jvm/java-8-oracle/jre/lib/management-agent.jar:/usr/lib/jvm/java-8-oracle/jre/lib/plugin.jar:/usr/lib/jvm/java-8-oracle/jre/lib/resources.jar:/usr/lib/jvm/java-8-oracle/jre/lib/rt.jar:/home/jcastro/delfos/xender/spark-single-user-rs/target/classes:/home/jcastro/.m2/repository/org/apache/spark/spark-core_2.11/2.1.0/spark-core_2.11-2.1.0.jar:/home/jcastro/.m2/repository/org/apache/avro/avro-mapred/1.7.7/avro-mapred-1.7.7-hadoop2.jar:/home/jcastro/.m2/repository/org/apache/avro/avro-ipc/1.7.7/avro-ipc-1.7.7.jar:/home/jcastro/.m2/repository/org/apache/avro/avro-ipc/1.7.7/avro-ipc-1.7.7-tests.jar:/home/jcastro/.m2/repository/com/twitter/chill_2.11/0.8.0/chill_2.11-0.8.0.jar:/home/jcastro/.m2/repository/com/esotericsoftware/kryo-shaded/3.0.3/kryo-shaded-3.0.3.jar:/home/jcastro/.m2/repository/com/esotericsoftware/minlog/1.3.0/minlog-1.3.0.jar:/home/jcastro/.m2/repository/org/objenesis/objenesis/2.1/objenesis-2.1.jar:/home/jcastro/.m2/repository/com/twitter/chill-java/0.8.0/chill-java-0.8.0.jar:/home/jcastro/.m2/repository/org/apache/xbean/xbean-asm5-shaded/4.4/xbean-asm5-shaded-4.4.jar:/home/jcastro/.m2/repository/org/apache/spark/spark-launcher_2.11/2.1.0/spark-launcher_2.11-2.1.0.jar:/home/jcastro/.m2/repository/org/apache/spark/spark-network-common_2.11/2.1.0/spark-network-common_2.11-2.1.0.jar:/home/jcastro/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.6.5/jackson-annotations-2.6.5.jar:/home/jcastro/.m2/repository/org/apache/spark/spark-network-shuffle_2.11/2.1.0/spark-network-shuffle_2.11-2.1.0.jar:/home/jcastro/.m2/repository/org/apache/spark/spark-unsafe_2.11/2.1.0/spark-unsafe_2.11-2.1.0.jar:/home/jcastro/.m2/repository/net/java/dev/jets3t/jets3t/0.7.1/jets3t-0.7.1.jar:/home/jcastro/.m2/repository/commons-httpclient/commons-httpclient/3.1/commons-httpclient-3.1.jar:/home/jcastro/.m2/repository/org/apache/curator/curator-recipes/2.4.0/curator-recipes-2.4.0.jar:/home/jcastro/.m2/repository/org/apache/curator/curator-framework/2.4.0/curator-framework-2.4.0.jar:/home/jcastro/.m2/repository/org/apache/zookeeper/zookeeper/3.4.5/zookeeper-3.4.5.jar:/home/jcastro/.m2/repository/javax/servlet/javax.servlet-api/3.1.0/javax.servlet-api-3.1.0.jar:/home/jcastro/.m2/repository/org/apache/commons/commons-lang3/3.5/commons-lang3-3.5.jar:/home/jcastro/.m2/repository/org/apache/commons/commons-math3/3.4.1/commons-math3-3.4.1.jar:/home/jcastro/.m2/repository/com/google/code/findbugs/jsr305/1.3.9/jsr305-1.3.9.jar:/home/jcastro/.m2/repository/org/slf4j/slf4j-api/1.7.16/slf4j-api-1.7.16.jar:/home/jcastro/.m2/repository/org/slf4j/jul-to-slf4j/1.7.16/jul-to-slf4j-1.7.16.jar:/home/jcastro/.m2/repository/org/slf4j/jcl-over-slf4j/1.7.16/jcl-over-slf4j-1.7.16.jar:/home/jcastro/.m2/repository/log4j/log4j/1.2.17/log4j-1.2.17.jar:/home/jcastro/.m2/repository/org/slf4j/slf4j-log4j12/1.7.16/slf4j-log4j12-1.7.16.jar:/home/jcastro/.m2/repository/com/ning/compress-lzf/1.0.3/compress-lzf-1.0.3.jar:/home/jcastro/.m2/repository/org/xerial/snappy/snappy-java/1.1.2.6/snappy-java-1.1.2.6.jar:/home/jcastro/.m2/repository/net/jpountz/lz4/lz4/1.3.0/lz4-1.3.0.jar:/home/jcastro/.m2/repository/org/roaringbitmap/RoaringBitmap/0.5.11/RoaringBitmap-0.5.11.jar:/home/jcastro/.m2/repository/commons-net/commons-net/2.2/commons-net-2.2.jar:/home/jcastro/.m2/repository/org/json4s/json4s-jackson_2.11/3.2.11/json4s-jackson_2.11-3.2.11.jar:/home/jcastro/.m2/repository/org/json4s/json4s-core_2.11/3.2.11/json4s-core_2.11-3.2.11.jar:/home/jcastro/.m2/repository/org/json4s/json4s-ast_2.11/3.2.11/json4s-ast_2.11-3.2.11.jar:/home/jcastro/.m2/repository/org/scala-lang/scalap/2.11.0/scalap-2.11.0.jar:/home/jcastro/.m2/repository/org/scala-lang/scala-compiler/2.11.0/scala-compiler-2.11.0.jar:/home/jcastro/.m2/repository/org/scala-lang/modules/scala-parser-combinators_2.11/1.0.1/scala-parser-combinators_2.11-1.0.1.jar:/home/jcastro/.m2/repository/org/glassfish/jersey/core/jersey-client/2.22.2/jersey-client-2.22.2.jar:/home/jcastro/.m2/repository/javax/ws/rs/javax.ws.rs-api/2.0.1/javax.ws.rs-api-2.0.1.jar:/home/jcastro/.m2/repository/org/glassfish/hk2/hk2-api/2.4.0-b34/hk2-api-2.4.0-b34.jar:/home/jcastro/.m2/repository/org/glassfish/hk2/hk2-utils/2.4.0-b34/hk2-utils-2.4.0-b34.jar:/home/jcastro/.m2/repository/org/glassfish/hk2/external/aopalliance-repackaged/2.4.0-b34/aopalliance-repackaged-2.4.0-b34.jar:/home/jcastro/.m2/repository/org/glassfish/hk2/external/javax.inject/2.4.0-b34/javax.inject-2.4.0-b34.jar:/home/jcastro/.m2/repository/org/glassfish/hk2/hk2-locator/2.4.0-b34/hk2-locator-2.4.0-b34.jar:/home/jcastro/.m2/repository/org/javassist/javassist/3.18.1-GA/javassist-3.18.1-GA.jar:/home/jcastro/.m2/repository/org/glassfish/jersey/core/jersey-common/2.22.2/jersey-common-2.22.2.jar:/home/jcastro/.m2/repository/javax/annotation/javax.annotation-api/1.2/javax.annotation-api-1.2.jar:/home/jcastro/.m2/repository/org/glassfish/jersey/bundles/repackaged/jersey-guava/2.22.2/jersey-guava-2.22.2.jar:/home/jcastro/.m2/repository/org/glassfish/hk2/osgi-resource-locator/1.0.1/osgi-resource-locator-1.0.1.jar:/home/jcastro/.m2/repository/org/glassfish/jersey/core/jersey-server/2.22.2/jersey-server-2.22.2.jar:/home/jcastro/.m2/repository/org/glassfish/jersey/media/jersey-media-jaxb/2.22.2/jersey-media-jaxb-2.22.2.jar:/home/jcastro/.m2/repository/javax/validation/validation-api/1.1.0.Final/validation-api-1.1.0.Final.jar:/home/jcastro/.m2/repository/org/glassfish/jersey/containers/jersey-container-servlet/2.22.2/jersey-container-servlet-2.22.2.jar:/home/jcastro/.m2/repository/org/glassfish/jersey/containers/jersey-container-servlet-core/2.22.2/jersey-container-servlet-core-2.22.2.jar:/home/jcastro/.m2/repository/io/netty/netty-all/4.0.42.Final/netty-all-4.0.42.Final.jar:/home/jcastro/.m2/repository/io/netty/netty/3.8.0.Final/netty-3.8.0.Final.jar:/home/jcastro/.m2/repository/com/clearspring/analytics/stream/2.7.0/stream-2.7.0.jar:/home/jcastro/.m2/repository/io/dropwizard/metrics/metrics-core/3.1.2/metrics-core-3.1.2.jar:/home/jcastro/.m2/repository/io/dropwizard/metrics/metrics-jvm/3.1.2/metrics-jvm-3.1.2.jar:/home/jcastro/.m2/repository/io/dropwizard/metrics/metrics-json/3.1.2/metrics-json-3.1.2.jar:/home/jcastro/.m2/repository/io/dropwizard/metrics/metrics-graphite/3.1.2/metrics-graphite-3.1.2.jar:/home/jcastro/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.6.5/jackson-databind-2.6.5.jar:/home/jcastro/.m2/repository/com/fasterxml/jackson/module/jackson-module-scala_2.11/2.6.5/jackson-module-scala_2.11-2.6.5.jar:/home/jcastro/.m2/repository/org/scala-lang/scala-reflect/2.11.7/scala-reflect-2.11.7.jar:/home/jcastro/.m2/repository/com/fasterxml/jackson/module/jackson-module-paranamer/2.6.5/jackson-module-paranamer-2.6.5.jar:/home/jcastro/.m2/repository/org/apache/ivy/ivy/2.4.0/ivy-2.4.0.jar:/home/jcastro/.m2/repository/oro/oro/2.0.8/oro-2.0.8.jar:/home/jcastro/.m2/repository/net/razorvine/pyrolite/4.13/pyrolite-4.13.jar:/home/jcastro/.m2/repository/net/sf/py4j/py4j/0.10.4/py4j-0.10.4.jar:/home/jcastro/.m2/repository/org/apache/spark/spark-tags_2.11/2.1.0/spark-tags_2.11-2.1.0.jar:/home/jcastro/.m2/repository/org/scalatest/scalatest_2.11/2.2.6/scalatest_2.11-2.2.6.jar:/home/jcastro/.m2/repository/org/scala-lang/modules/scala-xml_2.11/1.0.2/scala-xml_2.11-1.0.2.jar:/home/jcastro/.m2/repository/org/apache/commons/commons-crypto/1.0.0/commons-crypto-1.0.0.jar:/home/jcastro/.m2/repository/org/spark-project/spark/unused/1.0.0/unused-1.0.0.jar:/home/jcastro/.m2/repository/org/apache/spark/spark-mllib_2.11/2.1.0/spark-mllib_2.11-2.1.0.jar:/home/jcastro/.m2/repository/org/apache/spark/spark-streaming_2.11/2.1.0/spark-streaming_2.11-2.1.0.jar:/home/jcastro/.m2/repository/org/apache/spark/spark-sql_2.11/2.1.0/spark-sql_2.11-2.1.0.jar:/home/jcastro/.m2/repository/com/univocity/univocity-parsers/2.2.1/univocity-parsers-2.2.1.jar:/home/jcastro/.m2/repository/org/apache/spark/spark-sketch_2.11/2.1.0/spark-sketch_2.11-2.1.0.jar:/home/jcastro/.m2/repository/org/apache/spark/spark-catalyst_2.11/2.1.0/spark-catalyst_2.11-2.1.0.jar:/home/jcastro/.m2/repository/org/codehaus/janino/janino/3.0.0/janino-3.0.0.jar:/home/jcastro/.m2/repository/org/codehaus/janino/commons-compiler/3.0.0/commons-compiler-3.0.0.jar:/home/jcastro/.m2/repository/org/antlr/antlr4-runtime/4.5.3/antlr4-runtime-4.5.3.jar:/home/jcastro/.m2/repository/org/apache/parquet/parquet-column/1.8.1/parquet-column-1.8.1.jar:/home/jcastro/.m2/repository/org/apache/parquet/parquet-common/1.8.1/parquet-common-1.8.1.jar:/home/jcastro/.m2/repository/org/apache/parquet/parquet-encoding/1.8.1/parquet-encoding-1.8.1.jar:/home/jcastro/.m2/repository/org/apache/parquet/parquet-hadoop/1.8.1/parquet-hadoop-1.8.1.jar:/home/jcastro/.m2/repository/org/apache/parquet/parquet-format/2.3.0-incubating/parquet-format-2.3.0-incubating.jar:/home/jcastro/.m2/repository/org/apache/parquet/parquet-jackson/1.8.1/parquet-jackson-1.8.1.jar:/home/jcastro/.m2/repository/org/apache/spark/spark-graphx_2.11/2.1.0/spark-graphx_2.11-2.1.0.jar:/home/jcastro/.m2/repository/com/github/fommil/netlib/core/1.1.2/core-1.1.2.jar:/home/jcastro/.m2/repository/net/sourceforge/f2j/arpack_combined_all/0.1/arpack_combined_all-0.1.jar:/home/jcastro/.m2/repository/org/apache/spark/spark-mllib-local_2.11/2.1.0/spark-mllib-local_2.11-2.1.0.jar:/home/jcastro/.m2/repository/org/scalanlp/breeze_2.11/0.12/breeze_2.11-0.12.jar:/home/jcastro/.m2/repository/org/scalanlp/breeze-macros_2.11/0.12/breeze-macros_2.11-0.12.jar:/home/jcastro/.m2/repository/net/sf/opencsv/opencsv/2.3/opencsv-2.3.jar:/home/jcastro/.m2/repository/com/github/rwl/jtransforms/2.4.0/jtransforms-2.4.0.jar:/home/jcastro/.m2/repository/org/spire-math/spire_2.11/0.7.4/spire_2.11-0.7.4.jar:/home/jcastro/.m2/repository/org/spire-math/spire-macros_2.11/0.7.4/spire-macros_2.11-0.7.4.jar:/home/jcastro/.m2/repository/com/chuusai/shapeless_2.11/2.0.0/shapeless_2.11-2.0.0.jar:/home/jcastro/.m2/repository/org/jpmml/pmml-model/1.2.15/pmml-model-1.2.15.jar:/home/jcastro/.m2/repository/org/jpmml/pmml-schema/1.2.15/pmml-schema-1.2.15.jar:/home/jcastro/.m2/repository/org/scala-lang/scala-library/2.11.8/scala-library-2.11.8.jar:/home/jcastro/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.9.0.pr1/jackson-core-2.9.0.pr1.jar:/home/jcastro/.m2/repository/org/json4s/json4s-core_2.10/3.3.0/json4s-core_2.10-3.3.0.jar:/home/jcastro/.m2/repository/org/json4s/json4s-ast_2.10/3.3.0/json4s-ast_2.10-3.3.0.jar:/home/jcastro/.m2/repository/org/json4s/json4s-scalap_2.10/3.3.0/json4s-scalap_2.10-3.3.0.jar:/home/jcastro/.m2/repository/com/thoughtworks/paranamer/paranamer/2.8/paranamer-2.8.jar:/home/jcastro/.m2/repository/com/typesafe/config/1.3.1/config-1.3.1.jar:/home/jcastro/.m2/repository/com/google/code/gson/gson/2.8.0/gson-2.8.0.jar:/home/jcastro/.m2/repository/org/apache/hadoop/hadoop-hdfs/2.7.3/hadoop-hdfs-2.7.3.jar:/home/jcastro/.m2/repository/com/google/guava/guava/11.0.2/guava-11.0.2.jar:/home/jcastro/.m2/repository/org/mortbay/jetty/jetty/6.1.26/jetty-6.1.26.jar:/home/jcastro/.m2/repository/org/mortbay/jetty/jetty-util/6.1.26/jetty-util-6.1.26.jar:/home/jcastro/.m2/repository/com/sun/jersey/jersey-core/1.9/jersey-core-1.9.jar:/home/jcastro/.m2/repository/com/sun/jersey/jersey-server/1.9/jersey-server-1.9.jar:/home/jcastro/.m2/repository/asm/asm/3.1/asm-3.1.jar:/home/jcastro/.m2/repository/commons-cli/commons-cli/1.2/commons-cli-1.2.jar:/home/jcastro/.m2/repository/commons-codec/commons-codec/1.4/commons-codec-1.4.jar:/home/jcastro/.m2/repository/commons-io/commons-io/2.4/commons-io-2.4.jar:/home/jcastro/.m2/repository/commons-lang/commons-lang/2.6/commons-lang-2.6.jar:/home/jcastro/.m2/repository/commons-logging/commons-logging/1.1.3/commons-logging-1.1.3.jar:/home/jcastro/.m2/repository/commons-daemon/commons-daemon/1.0.13/commons-daemon-1.0.13.jar:/home/jcastro/.m2/repository/com/google/protobuf/protobuf-java/2.5.0/protobuf-java-2.5.0.jar:/home/jcastro/.m2/repository/javax/servlet/servlet-api/2.5/servlet-api-2.5.jar:/home/jcastro/.m2/repository/org/codehaus/jackson/jackson-core-asl/1.9.13/jackson-core-asl-1.9.13.jar:/home/jcastro/.m2/repository/org/codehaus/jackson/jackson-mapper-asl/1.9.13/jackson-mapper-asl-1.9.13.jar:/home/jcastro/.m2/repository/xmlenc/xmlenc/0.52/xmlenc-0.52.jar:/home/jcastro/.m2/repository/xerces/xercesImpl/2.9.1/xercesImpl-2.9.1.jar:/home/jcastro/.m2/repository/xml-apis/xml-apis/1.3.04/xml-apis-1.3.04.jar:/home/jcastro/.m2/repository/org/apache/htrace/htrace-core/3.1.0-incubating/htrace-core-3.1.0-incubating.jar:/home/jcastro/.m2/repository/org/fusesource/leveldbjni/leveldbjni-all/1.8/leveldbjni-all-1.8.jar:/home/jcastro/.m2/repository/org/apache/hadoop/hadoop-client/2.7.3/hadoop-client-2.7.3.jar:/home/jcastro/.m2/repository/org/apache/hadoop/hadoop-common/2.7.3/hadoop-common-2.7.3.jar:/home/jcastro/.m2/repository/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar:/home/jcastro/.m2/repository/javax/servlet/jsp/jsp-api/2.1/jsp-api-2.1.jar:/home/jcastro/.m2/repository/commons-configuration/commons-configuration/1.6/commons-configuration-1.6.jar:/home/jcastro/.m2/repository/commons-digester/commons-digester/1.8/commons-digester-1.8.jar:/home/jcastro/.m2/repository/commons-beanutils/commons-beanutils/1.7.0/commons-beanutils-1.7.0.jar:/home/jcastro/.m2/repository/commons-beanutils/commons-beanutils-core/1.8.0/commons-beanutils-core-1.8.0.jar:/home/jcastro/.m2/repository/org/apache/avro/avro/1.7.4/avro-1.7.4.jar:/home/jcastro/.m2/repository/org/apache/hadoop/hadoop-auth/2.7.3/hadoop-auth-2.7.3.jar:/home/jcastro/.m2/repository/org/apache/httpcomponents/httpclient/4.2.5/httpclient-4.2.5.jar:/home/jcastro/.m2/repository/org/apache/httpcomponents/httpcore/4.2.4/httpcore-4.2.4.jar:/home/jcastro/.m2/repository/org/apache/directory/server/apacheds-kerberos-codec/2.0.0-M15/apacheds-kerberos-codec-2.0.0-M15.jar:/home/jcastro/.m2/repository/org/apache/directory/server/apacheds-i18n/2.0.0-M15/apacheds-i18n-2.0.0-M15.jar:/home/jcastro/.m2/repository/org/apache/directory/api/api-asn1-api/1.0.0-M20/api-asn1-api-1.0.0-M20.jar:/home/jcastro/.m2/repository/org/apache/directory/api/api-util/1.0.0-M20/api-util-1.0.0-M20.jar:/home/jcastro/.m2/repository/org/apache/curator/curator-client/2.7.1/curator-client-2.7.1.jar:/home/jcastro/.m2/repository/org/apache/commons/commons-compress/1.4.1/commons-compress-1.4.1.jar:/home/jcastro/.m2/repository/org/tukaani/xz/1.0/xz-1.0.jar:/home/jcastro/.m2/repository/org/apache/hadoop/hadoop-mapreduce-client-app/2.7.3/hadoop-mapreduce-client-app-2.7.3.jar:/home/jcastro/.m2/repository/org/apache/hadoop/hadoop-mapreduce-client-common/2.7.3/hadoop-mapreduce-client-common-2.7.3.jar:/home/jcastro/.m2/repository/org/apache/hadoop/hadoop-yarn-client/2.7.3/hadoop-yarn-client-2.7.3.jar:/home/jcastro/.m2/repository/org/apache/hadoop/hadoop-yarn-server-common/2.7.3/hadoop-yarn-server-common-2.7.3.jar:/home/jcastro/.m2/repository/org/apache/hadoop/hadoop-mapreduce-client-shuffle/2.7.3/hadoop-mapreduce-client-shuffle-2.7.3.jar:/home/jcastro/.m2/repository/org/apache/hadoop/hadoop-yarn-api/2.7.3/hadoop-yarn-api-2.7.3.jar:/home/jcastro/.m2/repository/org/apache/hadoop/hadoop-mapreduce-client-core/2.7.3/hadoop-mapreduce-client-core-2.7.3.jar:/home/jcastro/.m2/repository/org/apache/hadoop/hadoop-yarn-common/2.7.3/hadoop-yarn-common-2.7.3.jar:/home/jcastro/.m2/repository/javax/xml/bind/jaxb-api/2.2.2/jaxb-api-2.2.2.jar:/home/jcastro/.m2/repository/javax/xml/stream/stax-api/1.0-2/stax-api-1.0-2.jar:/home/jcastro/.m2/repository/javax/activation/activation/1.1/activation-1.1.jar:/home/jcastro/.m2/repository/com/sun/jersey/jersey-client/1.9/jersey-client-1.9.jar:/home/jcastro/.m2/repository/org/codehaus/jackson/jackson-jaxrs/1.9.13/jackson-jaxrs-1.9.13.jar:/home/jcastro/.m2/repository/org/codehaus/jackson/jackson-xc/1.9.13/jackson-xc-1.9.13.jar:/home/jcastro/.m2/repository/org/apache/hadoop/hadoop-mapreduce-client-jobclient/2.7.3/hadoop-mapreduce-client-jobclient-2.7.3.jar:/home/jcastro/.m2/repository/org/apache/hadoop/hadoop-annotations/2.7.3/hadoop-annotations-2.7.3.jar \
		es.jcastro.delfos.scala.Main /home/jcastro/Dropbox/Datasets-new/ml-100k/u.data