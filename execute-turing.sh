#!/bin/bash

SPARK_BIN="/software/spark-2.1.0-bin-hadoop2.6/bin"

$SPARK_BIN/spark-submit --master spark://login.turing.ceatic.ujaen.es:7077 --class es.jcastro.delfos.scala.Main --conf "spark.local.dir=/home/sinbad2/spark-tmp/" target/original-spark-single-user-rs-1.0.jar --implicitFeedback --kRange 1 10 --checkpointDir "hdfs://192.168.10.27:8020/spark-single-user-grs/checkpoints" --numPartitions 320 --input hdfs://192.168.10.27:8020/user/sinbad2/datsets/netflix/u.data