#!/usr/bin/env bash

set -e

# init .git in example-bots if needed
# for i in $(ls -d ./example-bots/*/); do
#     pushd ${i%%/}
#     rm -rf .git
#     if [ ! -d .git ]; then
#         git init
#         git add .
#         git commit -m "initial"
#         # mvn package
#     fi
#     popd
#  done

LOG_FILE=service.log

# run the server and wait for it to start
echo "Server starting..."
echo "" > $LOG_FILE && fuser -k 8888/tcp && sleep 1
mvn spring-boot:run -pl xonix-mind-app > $LOG_FILE &
SERVER_PID=$!
( tail -f -n1 $LOG_FILE & ) | grep -q "Started XonixApplication"
echo "Server started (pid = $SERVER_PID)."
