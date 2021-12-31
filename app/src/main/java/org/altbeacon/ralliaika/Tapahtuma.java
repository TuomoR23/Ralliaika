package org.altbeacon.ralliaika;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;

public class Tapahtuma {
    protected static final String TAG = "Ranging2Activity";
    long lahtoaika = 0;
    long ekaaika = 0;
    long tokaaika = 0;
    String msg = "";

    public Tapahtuma() {

    }

    public Tapahtuma(long lahtoaika, long eka, long toka, String msg) {
        this.lahtoaika = lahtoaika;
        this.ekaaika = this.lahtoaika + eka;
        this.tokaaika = this.lahtoaika + toka;
        this.msg = msg;
    }

    @NonNull
    @Override
    public String toString() {
        return getDate(lahtoaika,"HH:mm:ss.SSS") + ", " + (ekaaika - lahtoaika) + ", 0" ;
    }

    public String toString(String format) {
        String ret = "";
        if (format.equals("kello"))
            ret = getDate(lahtoaika,"HH:mm:ss.SSS") + " "
                    + getDate(ekaaika,"HH:mm:ss.SSS") + " "
                    + getDate(tokaaika,"HH:mm:ss.SSS");
        else if (format.equals("tallennus"))
            ret = getDate(lahtoaika,"dd.MM.yyyy-HH:mm:ss,SS") + " "
                    + String.format("%.3f",((ekaaika - lahtoaika) / 1000.0f)) + " "
                    + String.format("%.3f",((0) / 1000.0f))
                    + " " + msg;

        return ret;
    }

    // 28.05.2021-13:10:30 14,388 16,214 28.05.2021-13:10:30 14,388 16,214 lkajsdf voiajsdf ölkjsdföklj asdf asfj
    public static Tapahtuma parse (String line) {
        String msg = "";
        long lahtoaika = 0;
        int eka = 0, toka = 0;
        String[] split = line.split(" ");

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss,SS");
        try {
            Date dt = formatter.parse(split[0]);
            lahtoaika = dt.getTime();
            String seka = split[1].replaceAll(",","");
            int kerroin = 1;
            if (!Character.isDigit(seka.charAt(0))) {
                kerroin = -1;
                seka = seka.substring(1);
            }
            eka = Integer.parseInt(seka) * kerroin;

            seka = split[2].replaceAll(",","");
            kerroin = 1;

            if (!Character.isDigit(seka.charAt(0))) {
                kerroin = -1;
                seka = seka.substring(1);
            }
            toka = Integer.parseInt(seka) ;
            msg = line.substring(split[0].length() + split[1].length() + split[2].length() + 2).trim();

          //  Log.e(TAG,"line:" + line + " " + getDate(lahtoaika,"dd.MM.yyyy-HH:mm:ss") + " eka:" + eka + " toka:"+ toka );
        }
        catch (ParseException e) {
            e.printStackTrace();
        }


        return new Tapahtuma(lahtoaika, eka, toka, msg);
    }
    /**
     * Return date in specified format.
     * @param milliSeconds Date in milliseconds
     * @param dateFormat Date format
     * @return String representing date in specified format
     */
    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
