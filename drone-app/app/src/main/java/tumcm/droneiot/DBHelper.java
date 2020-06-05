package tumcm.droneiot;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "flightlog.db";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE flight ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "locationx REAL, "+
                "locationy REAL, "+
                "obstacle INTEGER, "+
                "rssi INTEGER, "+
                "beaconmac TEXT, "+
                "timestamp TEXT, "+
                "batterycharge INTEGER)";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS flight");
        this.onCreate(db);
    }

    public void addLog(Double locationX, Double locationY, Integer obstacle, Integer rssi, String macAddress,String timestamp,Integer batterycharge){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("locationx", locationX);
        values.put("locationy", locationY);
        values.put("obstacle", obstacle);
        values.put("rssi", rssi);
        values.put("beaconmac", macAddress);
        values.put("timestamp",timestamp);
        values.put("batterycharge", batterycharge);
        db.insert("flight",
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
        db.close();
    }
}
