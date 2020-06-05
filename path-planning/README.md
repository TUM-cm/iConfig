# Path Planning

This program computes the paths to specific Wi-Fi/Bluetooth location, calculates the total travel distance and the total time.

## Prerequisites

In this Java project folder, a map.db file has to be created.

### map.db
The database needs seven columns in the form:
```
 id | class | locationx1 | locationy1 | locationx2 | locationy2 | mac
```
Class 1 represents a wall.
Class 3 represents a Wi-Fi/Bluetooth device.

One entry for a wall (from (9.5|7) to  (12|7)) could look like

```
34 | 1 | 9.5 | 7.0 | 12.0 | 7.0 | 
```

One entry for a device looks like the following:
```
36 | 3 | 39.4 | 7.3 | 0.0 | 0.0 | AB:4D:5F:GH:EH:23
```
Note that location 2 is set with 0.0.
## How to Start

This project just needs to be executed with the map.db file in the project root folder.
A new drone start position can be defined in the main class at the creation of a VisibilityGraph or Sampling-based graph: 

 new VisibilityGraph(data,new Point(0,1.0,6.0)); <-- Here the start location is set to (1|6).

## Output
The output will be printed in the console.
An example output could look like:

******Sampling Graph with Dijkstra******

Travelled across following coordinates: 1.0 6.0

Travelled across following coordinates: 20.0 1.0

Beacon number 1 configured after 30.77814375855755 with total travel distance of 19.6468827043885 meter.

Travelled across following coordinates: 20.0 1.0

Travelled across following coordinates: 22.0 3.0

Travelled across following coordinates: 24.0 5.0

Travelled across following coordinates: 26.0 7.0

Travelled across following coordinates: 28.0 9.0

Travelled across following coordinates: 32.0 23.0

Beacon number 2 configured after 65.61976044592505 with total travel distance of 45.5208109819343 meter.

Travelled across following coordinates: 32.0 23.0

Travelled across following coordinates: 42.0 22.0

Beacon number 3 configured after 84.78764717558273 with total travel distance of 55.57068660305519 meter.

Travelled across following coordinates: 42.0 22.0

Travelled across following coordinates: 50.0 1.0

Beacon number 4 configured after 118.12369379925491 with total travel distance of 78.04289165729942 meter.

Travelled across following coordinates: 50.0 1.0

Travelled across following coordinates: 65.0 20.0

Beacon number 5 configured after 152.70385286047704 with total travel distance of 102.25032853111983 meter.

Travelled across following coordinates: 65.0 20.0

Travelled across following coordinates: 79.0 1.0

Beacon number 6 configured after 186.89843856670888 with total travel distance of 125.85117597353172 meter.

Travelled across following coordinates: 79.0 1.0

Travelled across following coordinates: 95.0 17.0

Beacon number 7 configured after 217.2462392266946 with total travel distance of 148.47859297150126 meter.

Travelled across following coordinates: 95.0 17.0

Travelled across following coordinates: 125.0 1.0

Beacon number 8 configured after 262.107805200717 with total travel distance of 182.47859297150126 meter.



