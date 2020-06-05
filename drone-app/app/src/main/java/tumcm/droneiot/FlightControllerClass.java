package tumcm.droneiot;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.bluvision.beeks.sdk.domainobjects.Beacon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import dji.common.battery.BatteryState;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.VisionControlState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mobilerc.MobileRemoteController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.log;


public class FlightControllerClass {

    SurroundingBeacons beaconSurrounding = SurroundingBeacons.getInstance();
    IndoorLocation indoorLocation = IndoorLocation.getInstance();
    DBHelper db;
    private MobileRemoteController mobileRemoteController;
    private ObstacleDistance obstacleDistanceController;
    private int batteryCharge = 100;

    public void init(Button startButton, Context context) {
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        executeFlightAlgorithm();
                    }
                };
                thread.start();
            }
        });
        db = new DBHelper(context);
    }

    public void executeFlightAlgorithm() {
        obstacleDistanceController = new ObstacleDistance();
        obstacleDistanceController.initObstacleDistance();
        beaconSurrounding.clear();

        if ((DJISDKManager.getInstance().getProduct() instanceof Aircraft) && (null != DJISDKManager.getInstance().getProduct())) {
            Aircraft aircraft = (Aircraft) DJISDKManager.getInstance().getProduct();
            FlightController flightController = aircraft.getFlightController();
            if (flightController == null) {
                return;
            }

            try {
                mobileRemoteController =
                        aircraft.getMobileRemoteController();
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            try {
                flightController.getFlightAssistant().setActiveObstacleAvoidanceEnabled(false, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                    }
                });

            } catch (Exception exception) {
                exception.printStackTrace();
            }

            try {
                flightController.getFlightAssistant().setAdvancedPilotAssistanceSystemEnabled(false, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                    }
                });

            } catch (Exception exception) {
                exception.printStackTrace();
            }


            try {
                flightController.getFlightAssistant().setVisionAssistedPositioningEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                    }
                });

            } catch (Exception exception) {
                exception.printStackTrace();
            }

            //log first timestamp
            db.addLog(indoorLocation.getXPosition(),indoorLocation.getYPosition(),0,9999,"no beacon found", Calendar.getInstance().getTime().toString(),batteryCharge);

            //Start Flight Controller Algorithm
            flightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    return;
                }
            });
            extendDuration();
            mobileRemoteController.setRightStickVertical(0f);
            extendDuration();

            extendDuration();
            indoorLocation.startCallback(flightController);
            aircraft.getBattery().setStateCallback(new BatteryState.Callback() {
                @Override
                public void onUpdate(final BatteryState batteryState) {
                    if(batteryState == null) {return;}
                    batteryCharge = batteryState.getChargeRemainingInPercent();
                    Log.wtf("Battery Charge Remaining In Percent", Integer.toString(batteryCharge));
                }            });
            mobileRemoteController.setRightStickVertical(0.7f);


            do{
                do{
                Log.d("flying", "is flying");
                    beaconSurrounding.clear();
                    extendDuration();
                    logBeacons();
                } while (!(flightController.getState().getVelocityY() == 0.0f) && !(flightController.getState().getVelocityY() == 0.0f));


                mobileRemoteController.setRightStickVertical(0f);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                db.addLog(indoorLocation.getXPosition(),indoorLocation.getYPosition(),1,9999,"not measured", Calendar.getInstance().getTime().toString(),batteryCharge);


                do{
                    Log.d("turning", "finding next heading to fly at");
                    mobileRemoteController.setRightStickVertical(0f);
                    logBeacons();
                    mobileRemoteController.setLeftStickHorizontal(0.3f);
                    extendDuration();
                    mobileRemoteController.setLeftStickHorizontal(0f);
                    extendShortDuration();
                    mobileRemoteController.setRightStickVertical(0.7f);
                    extendDuration();
                    extendShortDuration();

                } while ((flightController.getState().getVelocityY() == 0.0f) && (flightController.getState().getVelocityY() == 0.0f));
                } while (batteryCharge > 0);
                    // was 35


                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mobileRemoteController.setRightStickVertical(0f);

                //return to home method
                int homeHeading = calculateHomeHeading(indoorLocation.getXPosition(),indoorLocation.getYPosition());
                do {
                    mobileRemoteController.setLeftStickHorizontal(0.2f);
                    }
                while(!(Math.round(flightController.getCompass().getHeading()) == homeHeading));
                mobileRemoteController.setLeftStickHorizontal(0);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int timeOutCounter = 0;
                do {
                    mobileRemoteController.setRightStickVertical(0.7f);
                    extendDuration();
                    timeOutCounter = timeOutCounter + 1;
                }
                while(((abs(indoorLocation.getXPosition()) < 2) && (abs(indoorLocation.getYPosition()) < 2)) || (timeOutCounter > 50));

            try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            mobileRemoteController.setRightStickVertical(0f);

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

                flightController.startLanding(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                    }
                });
            } else {

                return;
            }
        }

    private Integer calculateHomeHeading(double xPosition, double yPosition) {
        Double homeHeading;
        if (xPosition == 0) {xPosition = xPosition + 0.000000001;}
        if (yPosition == 0) {yPosition = yPosition + 0.000000001;}

        double phi = atan((xPosition/yPosition));

        if (yPosition < 0) {
            if (phi < 0){
                homeHeading = 360 + (phi*(360/(2*PI)));
            }
            else{
                homeHeading = phi*(360/(2*PI));
            }
        }
        else {
            phi = phi +  PI;
            homeHeading = phi*(360/(2*PI));
        }

        if (homeHeading > 180){
            homeHeading = (-1 * (360 - homeHeading));
        }

        return homeHeading.intValue();
    }

    private void logBeacons() {
        if (!beaconSurrounding.isEmpty()) {
            for (Map.Entry<String, Integer> entry : beaconSurrounding.entrySet()) {  // Iterate through hashmap
                String mac = entry.getKey();
                int rssi = entry.getValue();
                db.addLog(indoorLocation.getXPosition(),indoorLocation.getYPosition(),0,rssi,mac,Calendar.getInstance().getTime().toString(),batteryCharge);
            }
        }
        else{
            db.addLog(indoorLocation.getXPosition(),indoorLocation.getYPosition(),0,9999,"no beacon found",Calendar.getInstance().getTime().toString(),batteryCharge);
        }
    }

        private void extendDuration() {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void extendShortDuration() {
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
