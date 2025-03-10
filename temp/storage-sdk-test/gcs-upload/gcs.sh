#!/bin/bash

if [ -z "$1" ]; then
  echo "Usage: ./gcs.sh <upload|download>"
  exit 1
fi

ACTION="$1"

export GCP_PROJECT_ID=""
export GCP_BUCKET_NAME=""
export GCP_OBJECT_NAME="test.txt"
export FILE_PATH=""
export DEST_FILE_PATH="downloaded_test.txt"

MAVEN_COMMAND=""

case "$ACTION" in
  "upload")
    MAVEN_COMMAND="mvn exec:java -Dexec.mainClass=com.example.GCSUploader -Dexec.args=\"upload\""
    ;;
  "download")
    MAVEN_COMMAND="mvn exec:java -Dexec.mainClass=com.example.GCSDownloader -Dexec.args=\"download\""
    ;;
  *)
    echo "Invalid action: $ACTION.  Must be 'upload' or 'download'."
    exit 1
    ;;
esac

echo "Executing: $MAVEN_COMMAND"
eval $MAVEN_COMMAND
