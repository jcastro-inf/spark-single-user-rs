#!/bin/bash

export SPARK_HOME="/home/jcastro/Descargas/spark-2.1.0-bin-hadoop2.7"

$SPARK_HOME/bin/spark-submit --master "local[*]" \
                   --class es.jcastro.delfos.scala.Main \
                   ~/delfos/xender/spark-single-user-rs/target/original-spark-single-user-rs-1.0.jar \
                   --implicitFeedback --kRange  1 10 \
                   --input /home/jcastro/Dropbox/Datasets-new/ml-100k/u.data
