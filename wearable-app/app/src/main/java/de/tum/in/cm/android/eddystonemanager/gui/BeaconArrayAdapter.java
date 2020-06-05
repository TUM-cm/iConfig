package de.tum.in.cm.android.eddystonemanager.gui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import de.tum.in.cm.android.eddystonemanager.R;
import de.tum.in.cm.android.eddystonemanager.data.BeaconUnregistered;

public class BeaconArrayAdapter extends ArrayAdapter<BeaconUnregistered> {

    private final List<BeaconUnregistered> beacons;

    public BeaconArrayAdapter(Context context, int resource, List<BeaconUnregistered> beacons) {
        super(context, resource, beacons);
        this.beacons = beacons;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.beacon_list_item, parent, false);
        }
        BeaconUnregistered beaconUnregistered = getItem(position);
        TextView macAddressField = (TextView) convertView.findViewById(R.id.macAddress);
        macAddressField.setText(beaconUnregistered.getMac());
        TextView sBeaconIdField = (TextView) convertView.findViewById(R.id.sBeaconId);
        sBeaconIdField.setText(beaconUnregistered.getSBeaconId());
        TextView rssiField = (TextView) convertView.findViewById(R.id.rssi);
        rssiField.setText(String.valueOf(beaconUnregistered.getRssiFilter().getRssi()));
        return convertView;
    }

    @Override
    public int getCount() {
        return getBeacons().size();
    }

    @Override
    public BeaconUnregistered getItem(int position) {
        return getBeacons().get(position);
    }

    public List<BeaconUnregistered> getBeacons() {
        return this.beacons;
    }

}