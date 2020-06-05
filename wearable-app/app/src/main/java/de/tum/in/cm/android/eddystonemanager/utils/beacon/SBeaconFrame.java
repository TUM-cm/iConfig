package de.tum.in.cm.android.eddystonemanager.utils.beacon;

public class SBeaconFrame {

    private final String id;

    public SBeaconFrame(String id) {
        this.id = id;
    }

    public static SBeaconFrame parseData(byte[] frameBytes) {
        byte[] scanRecord = frameBytes;
        int startIndex = 0;
        int i = 0;
        int maximumTimeout = 255;
        while (i < scanRecord.length) {
            if ((scanRecord[i] & maximumTimeout) == 249 &&
                    (scanRecord[i + 1] & maximumTimeout) == 0) {
                startIndex = i;
                break;
            }
            i++;
        }
        String id;
        if ((scanRecord[startIndex + 2] & maximumTimeout) == 1 ||
                (scanRecord[startIndex + 2] & maximumTimeout) == 3) {
            byte[] reversedBytes = new byte[8];
            byte[] sidBytes = new byte[8];
            System.arraycopy(scanRecord, startIndex + 3, reversedBytes, 0, 8);
            int srcIndex = 7;
            int destIndex = 0;
            while (destIndex < 8) {
                sidBytes[destIndex] = reversedBytes[srcIndex];
                destIndex++;
                srcIndex--;
            }
            id = BeaconUtils.byteArrayToHex(sidBytes);
        } else {
            id = "N/A";
        }
        return new SBeaconFrame(id);
    }

    public String getId() {
        return this.id;
    }

}
