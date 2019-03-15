#!/usr/bin/env bash

druidCoordinatorIP="13.0.0.4"
dataSourceName="content-model-snapshot"
today=`date +%Y-%m-%d`
interval="1970-01-01_$today"
now=`date +%Y-%m-%d-%s`

spark/bin/spark-submit \
--conf spark.driver.extraJavaOptions="-Dconfig.file=/home/ops/adhoc-spark-scripts/resources/ESToCloudUploader.conf" \
--class org.ekstep.analytics.jobs.ESToCloudUploader \
/home/ops/adhoc-spark-scripts/adhoc-jobs-1.0.jar

# submit task to start batch ingestion
curl -X 'POST' -H 'Content-Type:application/json' -d @/home/ops/adhoc-spark-scripts/druid_models/content_index_batch.json http://${druidCoordinatorIP}:8090/druid/indexer/v1/task
# disable older segments
curl -X 'DELETE' -H 'Content-Type:application/json' http://${druidCoordinatorIP}:8081/druid/coordinator/v1/datasources/${dataSourceName}/intervals/${interval}

# delete older segments
curl -X 'POST' -H 'Content-Type:application/json' http://${druidCoordinatorIP}:8081/druid/indexer/v1/task -d '{
    "type": "kill",
    "id": "delete-older-segments-'${now}'",
    "dataSource": "'${dataSourceName}'",
    "interval" : "1970-01-01/'${today}'"
}'
