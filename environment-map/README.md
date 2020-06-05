# Map Postprocessing

This program is designed to visualize and postprocess the raw flightlog.db which was created during exploration flight.

## Prerequisites

Edit the url link in the connect() method to the .db file.

### flightlog.db
In addition to the automatically created 'flight' database, a 'raspberry' database needs to be added to flightlog.db. The form of this database need four columns in the form:
```
id | time | RSSI | BSSID
```
One entry could look like

```
10 | 14:03:07 | 43 | 34:81:c4:18:5a:bc
```

## How to Start

The program just needs to get executed.

## Output

In the executed a processedmap.txt will be created.
The output can be visualized with the help of plot.py.