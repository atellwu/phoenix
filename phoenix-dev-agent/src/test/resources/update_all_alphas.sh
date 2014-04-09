#!/bin/bash

IP_BASE="10.1.77."
PORT=58422
USER="root"
PWD="12qwaszx"
IP_START=193
IP_END=193

if [ $# != 1 ] ; then 
    echo "USAGE: $0 war_path" 
    echo " e.g.: $0 http://192.168.22.158:8000/phoenix-dev-agent.war " 
    exit 1; 
fi 

for (( i=$IP_START; i<=$IP_END; i++ ))
do
    ./deploy.sh $IP_BASE$i $PORT $USER $PWD $1 &
done

wait

echo "ALL DONE"
