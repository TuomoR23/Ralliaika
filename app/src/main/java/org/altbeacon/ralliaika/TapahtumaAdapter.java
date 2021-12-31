package org.altbeacon.ralliaika;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
        TextView tvViesti = (TextView) convertView.findViewById(R.id.tvMsg);

        if (debug.lahtoaika > 0) tvLahto.setText(Tapahtuma.getDate(debug.lahtoaika,"HH:mm:ss,SS"));
        else tvLahto.setText("");

        long aika = 0;
        if (debug.lahtoaika > 0 && debug.ekaaika > 0) {
            aika = debug.ekaaika - debug.lahtoaika;
            // Muotoillaan nätiksi
            tvEka.setText(millistoSTR(aika));
        }
        else tvEka.setText("");

        tvViesti.setText(debug.msg);


        return convertView;
    }
    private String millistoSTR (long millis) {
        String ret = "";
        if (millis > 0) {
            long intMillis = millis;
            long mm = TimeUnit.MILLISECONDS.toMinutes(intMillis);
            intMillis -= TimeUnit.MINUTES.toMillis(mm);
            long ss = TimeUnit.MILLISECONDS.toSeconds(intMillis);
            intMillis -= TimeUnit.SECONDS.toMillis(ss);

            String stringInterval = "%d:%02d,%03d";

            ret = String.format(stringInterval , mm, ss, intMillis);
            ret = ret.substring(0,ret.length() - 1);
        }

        return ret;
    }
}
