#!/bin/bash

spark-submit --master "local[*]" \
                   --class es.jcastro.delfos.scala.Main \
                   ~/delfos/xender/spark-single-user-rs/target/original-spark-single-user-rs-1.0.jar \
                   --makeImplicit --kRange  1 10 \
                   --input /home/jcastro/Dropbox/Datasets-new/ml-100k/u.data 
