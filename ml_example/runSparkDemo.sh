#!/bin/sh

export SPARK_CLASSPATH=/etc/hbase/conf:/usr/hdp/current/hbase-client/lib/hbase-protocol.jar
/root/spark-1.3.1-bin-hadoop2.6/bin/spark-shell \
	--driver-memory 2048m --driver-java-options -Xss16m \
	--executor-memory 2048m --conf hdp.version=$HDP_VER --jars \
	/usr/hdp/2.3.0.0-1938/phoenix/phoenix-4.4.0.2.3.0.0-1938-client.jar -i RecommendationEngine.scala
