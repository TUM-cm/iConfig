package tumcm.droneiot;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import dji.common.flightcontroller.FlightControllerState;
import dji.sdk.flightcontroller.FlightController;

public class IndoorLocation {
    private static IndoorLocation instance;
    private double xPosition = 0;
    private double yPosition = 0;
    private TextView textView = null;

    private IndoorLocation() {
    }

    public static IndoorLocation getInstance() {
        if (IndoorLocation.instance == null) {
            IndoorLocation.instance = new IndoorLocation();
        }
        return IndoorLocation.instance;
    }

    public void initializeUI(TextView indoorPositionTextView) {
        this.textView = indoorPositionTextView;
    }

    public void startCallback(FlightController flightController) {
        flightController.setStateCallback(new FlightControllerState.Callback() {
            @Override
            public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                updatePosition(flightControllerState.getVelocityX(),flightControllerState.getVelocityY());
            }
        });

    }

    public void updatePosition(float xVelocity, float yVelocity) {
        xPosition = (Double) xPosition + (xVelocity * 0.1);
        yPosition = (Double) yPosition + (yVelocity * 0.1);
        textView.setText("Indoor Position: " + xPosition + " " + yPosition);
    }

    public double getXPosition() {
        return xPosition;
    }

    public double getYPosition() {
        return yPosition;
    }

}