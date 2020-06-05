package tumcm.droneiot;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import dji.common.flightcontroller.ObstacleDetectionSector;
import dji.common.flightcontroller.VisionControlState;
import dji.common.flightcontroller.VisionDetectionState;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightAssistant;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.thirdparty.sanselan.formats.tiff.constants.TagInfo;

public class ObstacleDistance {
    public TextView sensorSection1;
    public TextView sensorSection2;
    //needed
    public double obstacleDistanceLeft;
    public double obstacleDistanceRight;

    public void initObstacleDistance() {
        if ((DJISDKManager.getInstance().getProduct() instanceof Aircraft) && (null != DJISDKManager.getInstance().getProduct())) {
            FlightController flightController =
                    ((Aircraft) DJISDKManager.getInstance().getProduct()).getFlightController();

            final FlightAssistant intelligentFlightAssistant = flightController.getFlightAssistant();

            if (intelligentFlightAssistant != null) {

                intelligentFlightAssistant.setVisionDetectionStateUpdatedCallback(new VisionDetectionState.Callback() {
                    @Override
                    public void onUpdate(@NonNull VisionDetectionState visionDetectionState) {

                        ObstacleDetectionSector[] visionDetectionSectorArray =
                                visionDetectionState.getDetectionSectors();

                        obstacleDistanceLeft = visionDetectionSectorArray[0].getObstacleDistanceInMeters();
                        obstacleDistanceRight = visionDetectionSectorArray[1].getObstacleDistanceInMeters();
                    }
                });
            }
        }
    }

    public void printObstacleDistance(TextView sensorsection1, TextView sensorsection2) {
        sensorSection1 = sensorsection1;
        sensorSection2 = sensorsection2;

        if ((DJISDKManager.getInstance().getProduct() instanceof Aircraft) && (null != DJISDKManager.getInstance().getProduct())) {
            FlightController flightController =
                    ((Aircraft) DJISDKManager.getInstance().getProduct()).getFlightController();

            final FlightAssistant intelligentFlightAssistant = flightController.getFlightAssistant();

            if (intelligentFlightAssistant != null) {

                intelligentFlightAssistant.setVisionDetectionStateUpdatedCallback(new VisionDetectionState.Callback() {
                    @Override
                    public void onUpdate(@NonNull VisionDetectionState visionDetectionState) {
                        StringBuilder stringBuilder1 = new StringBuilder();
                        StringBuilder stringBuilder2 = new StringBuilder();
                        StringBuilder stringBuilder3 = new StringBuilder();

                        ObstacleDetectionSector[] visionDetectionSectorArray =
                                visionDetectionState.getDetectionSectors();
                        //print Sector Forward Left
                            visionDetectionSectorArray[0].getObstacleDistanceInMeters();
                            visionDetectionSectorArray[0].getWarningLevel();

                            stringBuilder1.append("Obstacle distance: ")
                                    .append("\n")
                                    .append(visionDetectionSectorArray[0].getObstacleDistanceInMeters())
                                    .append("\n");
                            stringBuilder1.append("Distance warning: ")
                                    .append("\n")
                                    .append(visionDetectionSectorArray[0].getWarningLevel())
                                    .append("\n");

                        sensorSection1.setText(stringBuilder1.toString());

                        //print Sector Forward Right
                        visionDetectionSectorArray[1].getObstacleDistanceInMeters();
                        visionDetectionSectorArray[1].getWarningLevel();

                        stringBuilder2.append("Obstacle distance: ")
                                .append("\n")
                                .append(visionDetectionSectorArray[1].getObstacleDistanceInMeters())
                                .append("\n");
                        stringBuilder2.append("Distance warning: ")
                                .append("\n")
                                .append(visionDetectionSectorArray[1].getWarningLevel())
                                .append("\n");

                        sensorSection2.setText(stringBuilder2.toString());

                        //print Sector Rear Left
                        visionDetectionSectorArray[2].getObstacleDistanceInMeters();
                        visionDetectionSectorArray[2].getWarningLevel();

                        stringBuilder3.append("Obstacle distance: ")
                                .append(visionDetectionSectorArray[2].getObstacleDistanceInMeters())
                                .append("\n");
                        stringBuilder3.append("Distance warning: ")
                                .append(visionDetectionSectorArray[2].getWarningLevel())
                                .append("\n");

                    }
                });
            }
        } else {
            Log.i("printObstacleDistance", "onAttachedToWindow FC NOT Available");
        }
    }

    public double getObstacleDistanceLeft() {
        return obstacleDistanceLeft;
    }

    public double getObstacleDistanceRight() {
        return obstacleDistanceRight;
    }
}
