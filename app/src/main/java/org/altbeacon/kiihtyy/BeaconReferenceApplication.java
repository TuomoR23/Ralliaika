package org.altbeacon.ralliaika;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by dyoung on 12/13/13.
 */
public class BeaconReferenceApplication extends Application implements BootstrapNotifier {
    private static final String TAG = "BeaconReferenceApp";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private MonitoringActivity monitoringActivity = null;
    private String cumulativeLog = "";
    ArrayList<Region> regions = new ArrayList<>();
    int aktiivinen = 1;

    public long viimeisinAika1 = 0, viimeisinAika2 = 0;

    public void onCreate() {
        Log.e(TAG,"onCreate");
        super.onCreate();
        BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        regions.add(new Region("backgroundRegion",Identifier.parse("0x45203d"), null, Identifier.parse("4660")));
        regions.add(new Region("backgroundRegion",Identifier.parse("0x45203d"), null, Identifier.parse("4661")));

        // By default the AndroidBeaconLibrary will only find AltBeacons.  If you wish to make it
        // find a different type of beacon, you must specify the byte layout for that beacon's
        // advertisement with a line like below.  The example shows how to find a beacon with the
        // same byte layout as AltBeacon but with a beaconTypeCode of 0xaabb.  To find the proper
        // layout expression for other beacon types, do a web search for "setBeaconLayout"
        // including the quotes.
        //
        //beaconManager.getBeaconParsers().clear();
        //beaconManager.getBeaconParsers().add(new BeaconParser().
        //        setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25")); // AltBeacon
        //4c000215
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:0-3=6a110208,i:4-6,i:7-8,i:9-10,p:11-11")); // Oma tunniste, lyhennetty
        //setBeaconLayout("m:0-3=6a110215,i:4-19,i:20-21,i:22-23,p:24-24")); // Oma tunniste
  //              setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")); // IBeacon
        // ID1: Tätä ei käytetä
        // ID2: akun prosentit
        // ID3: ponderin tunniste
//0201040dff6a110208 416a61 00 31 0401 c3

     beaconManager.setDebug(true);

        Log.e(TAG,"onCreate parseri lisätty");

        // Uncomment the code below to use a foreground service to scan for beacons. This unlocks
        // the ability to continually scan for long periods of time in the background on Andorid 8+
        // in exchange for showing an icon at the top of the screen and a always-on notification to
        // communicate to users that your app is using resources in the background.
        //

        /*
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle("Scanning for Beacons");
        Intent intent = new Intent(this, MonitoringActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification Channel ID",
                    "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Notification Channel Description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);

        // For the above foreground scanning service to be useful, you need to disable
        // JobScheduler-based scans (used on Android 8+) and set a fast background scan
        // cycle that would otherwise be disallowed by the operating system.
        //
        beaconManager.setEnableScheduledScanJobs(false);
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(1100);
        */



        Log.d(TAG, "setting up background monitoring for beacons and power saving");
        // wake up the app when a beacon is seen
        //enableMonitoring();
//        Region region = new Region("backgroundRegion",
//                //Identifier.parse("0x45203d"), null, null);
//                Identifier.parse("0x45203d"), null, Identifier.parse("4661"));
//        regionBootstrap = new RegionBootstrap(this, region);

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the BeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);

        // If you wish to test beacon detection in the Android Emulator, you can use code like this:
        // BeaconManager.setBeaconSimulator(new TimedBeaconSimulator() );
        // ((TimedBeaconSimulator) BeaconManager.getBeaconSimulator()).createTimedSimulatedBeacons();
    }

    public void disableMonitoring() {
        if (regionBootstrap != null) {
            regionBootstrap.disable();
            regionBootstrap = null;
        }
        viimeisinAika1 = 0;
        viimeisinAika2 = 0;
    }
    public void enableMonitoring() {
        viimeisinAika1 = 0;
        viimeisinAika2 = 0;

//        Region region = new Region("backgroundRegion",
//                Identifier.parse("0x45203d"), null, Identifier.parse("4660"));
                //null, null, null);
                //Identifier.parse("0x45203d"), null, null);
//                Identifier.parse("0x45203d"), null, Identifier.parse("4661"));
        //regionBootstrap = new RegionBootstrap(this, region);
        regionBootstrap = new RegionBootstrap(this, regions.get(0));
        aktiivinen = 1;
    }
    public boolean isMonitoringOn () {
        return !(regionBootstrap == null);
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

    @Override
    public void didEnterRegion(Region arg0) {
        if (aktiivinen == 1)
            viimeisinAika1 = System.currentTimeMillis();
        else
            viimeisinAika2 = System.currentTimeMillis();

        Log.e(TAG, "didEnterRegion arg0:" + arg0 + " " + getDate(viimeisinAika1,"HH:mm:ss.SSS")
                + " " + getDate(viimeisinAika2,"HH:mm:ss.SSS"));
        // Send a notification to the user whenever a Beacon
        // matching a Region (defined above) are first seen.
        //Log.d(TAG, "Sending notification.");

        //sendNotification();
        if (monitoringActivity != null) {
            // If the Monitoring Activity is visible, we log info about the beacons we have
            // seen on its display
            logToDisplay("I see a beacon again" );
        }
        Region region = new Region("backgroundRegion",
//                Identifier.parse("0x416a61"), null, null);
        //null, null, null);
//        Identifier.parse("0x45203d"), null, null);
                Identifier.parse("0x45203d"), null, Identifier.parse("4661"));
//        regionBootstrap = new RegionBootstrap(this, region);
        regionBootstrap.disable();
        if (aktiivinen < 2)
            regionBootstrap = new RegionBootstrap(this, regions.get(aktiivinen));
        //else
          //  disableMonitoring();
        aktiivinen = 2;
    }


    @Override
    public void didExitRegion(Region region) {
        Log.e(TAG,"didExitRegion");
        logToDisplay("I no longer see a beacon.");
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        logToDisplay("Current region state is: " + (state == 1 ? "INSIDE" : "OUTSIDE ("+state+")"));
    }

    private void sendNotification() {
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Beacon Reference Notifications",
                    "Beacon Reference Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
            builder = new Notification.Builder(this, channel.getId());
        }
        else {
            builder = new Notification.Builder(this);
            builder.setPriority(Notification.PRIORITY_HIGH);
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MonitoringActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle("I detect a beacon");
        builder.setContentText("Tap here to see details in the reference app");
        builder.setContentIntent(resultPendingIntent);
        notificationManager.notify(1, builder.build());
    }

    public void setMonitoringActivity(MonitoringActivity activity) {
        this.monitoringActivity = activity;
    }

    private void logToDisplay(String line) {
        cumulativeLog += (line + "\n");
        if (this.monitoringActivity != null) {
            this.monitoringActivity.updateLog(cumulativeLog);
        }
    }

    public String getLog() {
        return cumulativeLog;
    }

}
