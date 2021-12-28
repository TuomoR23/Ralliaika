package org.altbeacon.ralliaika;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TapahtumaAdapter extends ArrayAdapter<Tapahtuma> {

    public TapahtumaAdapter(@NonNull Context context, ArrayList<Tapahtuma> tapahtumat) {
        super(context, 0, tapahtumat);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Tapahtuma debug = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.tapahtuma_item, parent, false);
        }
        TextView tvLahto = (TextView) convertView.findViewById(R.id.tvLahto);
        TextView tvEka = (TextView) convertView.findViewById(R.id.tvEka);
        TextView tvToka = (TextView) convertView.findViewById(R.id.tvToka);
        TextView tvViesti = (TextView) convertView.findViewById(R.id.tvMsg);

        if (debug.lahtoaika > 0) tvLahto.setText(Tapahtuma.getDate(debug.lahtoaika,"HH:mm:ss"));
        else tvLahto.setText("");

        long aika = 0;
        if (debug.lahtoaika > 0 && debug.ekaaika > 0) {
            aika = debug.ekaaika - debug.lahtoaika;
            tvEka.setText(String.format("%.3f",(aika / 1000.0f)));
        }
        else tvEka.setText("");

        aika = 0;
        if (debug.lahtoaika > 0 && debug.tokaaika > 0) {
            aika = debug.tokaaika - debug.lahtoaika;
            tvToka.setText(String.format("%.3f",(aika / 1000.0f)));
        }
        else tvToka.setText("");

        tvViesti.setText(debug.msg);


        return convertView;
    }
}
