#!/usr/bin/env bash

if [ ! -x "$(which http)" ]; then
    echo 'You need HTTPie to run this script (https://httpie.org/):'
    echo 'sudo pip3 install httpie'
    echo 'OR'
    echo 'sudo apt install httpie'
    exit 1
fi
# https://github.com/lineate/xonix-bot/tree/example-bot
# if no bots yet, add some
BOTS=`http GET "localhost:8888/match/all-bots" | grep -oP '"id":\K[0-9]+'`
if [ -z "${BOTS}" ]; then
    http POST "localhost:8888/match/add" name="bot" srcUrl="https://github.com/lineate/xonix-bot"
    http POST "localhost:8888/match/add" name="bot" srcUrl="https://github.com/lineate/kotlin-bot"
    http POST "localhost:8888/match/add" name="bot" srcUrl="https://github.com/lineate/scala-bot"
    http POST "localhost:8888/match/add" name="bot" srcUrl="https://github.com/lineate/scala-bot"
#    http POST "localhost:8888/match/add" name="bot" srcUrl="file://$PWD/../java-bot"
#    http POST "localhost:8888/match/add" name="bot" srcUrl="file://$PWD/../kotlin-bot"
#    http POST "localhost:8888/match/add" name="bot" srcUrl="file://$PWD/../scala-bot"
#    http POST "localhost:8888/match/add" name="bot" srcUrl="file://$PWD/../scala-bot"
else
    BOTS=`echo ${BOTS} | sed -e 's/ /, /g'`
    echo "Bots to run: ${BOTS}"
fi

MATCH_ID=`http POST 'localhost:8888/match/create' duration=1000 percent=90 | grep -oP '"id":\K[0-9]+'`
#clear
echo "" > logs/match-${MATCH_ID}.log
http POST "localhost:8888/match/$MATCH_ID/start" delay=10 skipBuild=false skipVideo=true
tail -f -n36 logs/match-${MATCH_ID}.log
# clear
http GET "localhost:8888/match/$MATCH_ID/state"
