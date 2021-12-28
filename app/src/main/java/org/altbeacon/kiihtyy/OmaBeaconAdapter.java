package org.altbeacon.ralliaika;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class OmaBeaconAdapter extends ArrayAdapter<OmaBeacon> {
    public OmaBeaconAdapter(Context context, ArrayList<OmaBeacon> omatbeaconit) {
        super(context, 0, omatbeaconit);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        OmaBeacon debug = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.omabeacon_item, parent, false);
        }
        // Lookup view for data population
        TextView tvID3 = (TextView) convertView.findViewById(R.id.tvID3);
        TextView tvID2 = (TextView) convertView.findViewById(R.id.tvID2);
        TextView tvRSSI = (TextView) convertView.findViewById(R.id.tvRSSI);
        TextView tvDist = (TextView) convertView.findViewById(R.id.tvDist);
        TextView tvLast = (TextView) convertView.findViewById(R.id.tvLast);

        //TextView tvDist
        // Populate the data into the template view using the data object
        String id3 = "";
        id3 = debug.getId3().toString();
        if (id3.equals("4660"))
            id3 = "1";
        if (id3.equals("4661"))
            id3 = "2";

        tvID3.setText(id3);
        tvID2.setText("" + debug.getId2().toInt()); //  Integer.decode("" + debug.getId2()).toString());
        tvRSSI.setText(debug.getRssi() + "");
        tvDist.setText(String.format("%.1f", debug.getDistance()));
        tvLast.setText(System.currentTimeMillis() - debug.getLastSeen() + "");
        //Log.e(TAG, "viestilkm:" + debug.getViestilkm());
//            if (debug.getViestiTullut())
//                tvLaite.setBackgroundColor(Color.GREEN);
//            else
//                tvLaite.setBackgroundColor(Color.RED);

        // Return the completed view to render on screen
        return convertView;
    }
}

