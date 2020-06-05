#!/bin/bash
OPT=${1}

CMD="python BeaconConfigWebserver.py"

if [ "$OPT" == "start" ]; then
	echo "Starting Beacon Config Webserver, logs can be found in beacon_webserver"
	rm beacon_webserver.out
	sudo nohup $CMD > beacon_webserver.out 2>&1 </dev/null &
	exit
fi

if [ "$OPT" == "stop" ]; then
	echo "Stopping Beacon Config Webserver"
	sudo pkill -f "$CMD"
fi
