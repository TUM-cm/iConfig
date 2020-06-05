#!/bin/sh

i=1
param="-t1 -c1 -d30s http://192.168.1.218/"
sleep_time="30s"

while [ "$i" -lt 11 ]
do
  echo "Round $i"
  
  file="file_1_MB"
  echo ${file}
  wrk ${param}${file} > result_${i}_${file}
  sleep ${sleep_time}

  file="file_10_MB"
  echo ${file}
  wrk ${param}${file} > result_${i}_${file}
  sleep ${sleep_time}

  file="file_50_MB"
  echo ${file}
  wrk ${param}${file} > result_${i}_${file}
  sleep ${sleep_time}

  file="file_100_MB"
  echo ${file}
  wrk ${param}${file} > result_${i}_${file}
  sleep ${sleep_time}

  file="file_500_MB"
  echo ${file}
  wrk ${param}${file} > result_${i}_${file}
  sleep ${sleep_time}

  file="file_1_GB"
  echo ${file}
  wrk ${param}${file} > result_${i}_${file}
  sleep ${sleep_time}

  sleep 1m
  i=$((i + 1))
done
