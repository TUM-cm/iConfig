# iConfig: Edge-Driven IoT Device Management using Wearables and Drones
PhD thesis *Edge-Driven Proximity Service Platform for the Internet of Things in Indoor Environments* of Michael Haus

This repository addresses the RQ2 of the research problem:<br/>
**How to effectively manage IoT deployments consisting of spatially distributed, heterogeneous, wireless IoT devices?**

We begin addressing this question by presenting the design and implementation of an IoT device management called
iConfig with a global device map for administration and multiple edge modules intended to run on user devices,
e.g., wearables, to interact with physically nearby IoT devices. Moreover, our usability study and testbed
experiments highlights the difference between manual and automated IoT device configuration. The video:
https://www.youtube.com/watch?v=PuqgJLW1Llw shows our working prototype.

The previous edge modules are mobile user-dependent dedicated for users interested in managing and 
interacting with IoT environments. We extend this work by introducing an additional edge module 
targeted for drones being completely independent from users to further reduce the operational costs for IoT 
device management. The drone control is independent of any ground control station, including two area 
exploration strategies tested in multiple indoor testbeds, particularly assessing when the exploration of an 
unknown territory is successfully completed. In addition, the optimization of the flight path using hot 
spots, i.e., cover multiple IoT devices at once without flying to each device individually, to find an 
optimum in the trade-off between limited flight time and maximum of discovered IoT devices.
