package de.tum.in.cm.android.eddystonemanager.utils.beacon;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public enum AdvertisementRatesConstants {

    Disabled(0.0f),
    Every_5_Seconds(0.2f),
    Every_3_3_Seconds(0.3f),
    Every_2_5_Seconds(0.4f),
    Every_2_Seconds(0.5f),
    Every_1_7_Seconds(0.6f),
    Every_1_4_Seconds(0.7f),
    Every_1_2_Seconds(0.8f),
    Every_1_1_Seconds(0.9f),
    Every_1_Second(1.0f),
    Two_Times_A_Second(2.0f),
    Three_Times_A_Second(3.0f),
    Five_Times_A_Second(5.0f),
    Ten_Times_A_Second(10.0f);

    private final float frequency;

    AdvertisementRatesConstants(float frequency) {
        this.frequency = frequency;
    }

    public float getFrequency() {
        return this.frequency;
    }

    public static float convertToFromDevice(float advStandard) {
        if (advStandard >= 1.0f) {
            advStandard = 1.0f / advStandard;
            if (advStandard < 1.0f) {
                DecimalFormat df = new DecimalFormat("#.#");
                df.setRoundingMode(RoundingMode.HALF_UP);
                advStandard = Float.parseFloat(df.format((double) advStandard));
            }
        } else if (advStandard != 0.0f) {
            advStandard = (float) ((int) (1.0f / advStandard));
        } else {
            advStandard = 0.0f;
        }
        return advStandard;
    }

    public static float convertToDevice(float value) {
        return 1.0f / value;
    }

}