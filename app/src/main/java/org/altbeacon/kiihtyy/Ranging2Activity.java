package org.altbeacon.ralliaika;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class Ranging2Activity extends Activity implements BeaconConsumer {
    protected static final String TAG = "Ranging2Activity";
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    private static final int VALI_REQUEST = 10;

    // Näissä kahdess listassa pitää olla ponderit samassa järjestyksessä
    ArrayList<OmaBeacon> omabeaconList = new ArrayList<>();
    ListView lBeaconit, lTapahtumat;
    OmaBeaconAdapter omabeaconadapter;
    TapahtumaAdapter tapahtumatadapter;

    private Button btnLahtoon;

    private ImageView img1, img2, img3, img4, img5;
    Canvas c;
    Paint paint = new Paint();
    private int height = 0, width = 0, iVali = 30;
    private TextView tvHH, tvMM, tvSS;
    private int oldHH = -1, oldMM = -1, oldSS = -1;
    private final Handler timeHandler = new Handler();
    private Runnable updateTimeTask;
    private Calendar now;
    private ListView llTapahtumat;
    BeaconReferenceApplication application;

    private Tapahtuma tapahtuma;
    private ArrayList<Tapahtuma> tapahtumalist = new ArrayList<>();

    String gFilebase = "kii";
    String gFilename = "";
    private int tila = 0; // 0 - perus, 1 - lahtoon, sytytetään valot, 2 - lahti, kun valot sammui
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging2);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // pidetään näyttö päällä
        PiilotaBaarit();
        img1 = (ImageView) findViewById((R.id.img1));
        img2 = (ImageView) findViewById((R.id.img2));
        img3 = (ImageView) findViewById((R.id.img3));
        img4 = (ImageView) findViewById((R.id.img4));
        img5 = (ImageView) findViewById((R.id.img5));

        tvHH = (TextView) findViewById(R.id.tvHH);
        tvMM = (TextView) findViewById(R.id.tvMM);
        tvSS = (TextView) findViewById(R.id.tvSS);

        btnLahtoon = (Button) findViewById(R.id.btnLahtoon);
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.FILL);
        omabeaconadapter = new OmaBeaconAdapter(this, omabeaconList);
        lBeaconit = (ListView) findViewById(R.id.listBeaconit);

        tapahtumatadapter = new TapahtumaAdapter(this,tapahtumalist);
        lTapahtumat = (ListView) findViewById(R.id.listTapahtumat);

        naytaLahtobutton();

        lBeaconit.setAdapter(omabeaconadapter);
        lTapahtumat.setAdapter(tapahtumatadapter);
        verifyBluetooth();

        final ViewTreeObserver obs = img1.getViewTreeObserver();
        obs.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                img1.getViewTreeObserver().removeOnPreDrawListener(this);
                Log.d(TAG, "onPreDraw tv height is " + img1.getHeight()); // bad for performance, remove on production
                height = img1.getHeight();
                width = img1.getWidth();

                Log.e(TAG, "height:" + height + " width:" + width);

                Bitmap newImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                c = new Canvas(newImage);

                img1.setImageBitmap(newImage);

                PiirraVali();
                return true;
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("This app needs background location access");
                            builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                                @TargetApi(23)
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                            PERMISSION_REQUEST_BACKGROUND_LOCATION);
                                }

                            });
                            builder.show();
                        } else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Functionality limited");
                            builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                }

                            });
                            builder.show();
                        }
                    }
                }
            } else {
                if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            PERMISSION_REQUEST_FINE_LOCATION);
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }

            }
        }
        now = Calendar.getInstance();

        updateTimeTask = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                long l = System.currentTimeMillis();
                now.setTimeInMillis(l);

                int hh = now.get(Calendar.HOUR_OF_DAY);
                int mm = now.get(Calendar.MINUTE);
                int ss = now.get(Calendar.SECOND);

                if (hh != oldHH)
                    tvHH.setText(String.format("%02d", hh));
                if (mm != oldMM)
                    tvMM.setText(String.format("%02d", mm));

                if (ss != oldSS)
                    tvSS.setText(String.format("%02d", ss));

                oldHH = hh;
                oldMM = mm;
                oldSS = ss;

                if (tila == 1) {
                    if (ss == 55) {
                        sytyta(1);
                    } else if (ss == 56) {
                        sytyta(2);
                    } else if (ss == 57) {
                        sytyta(3);
                    } else if (ss == 58) {
                        sytyta(4);
                    } else if (ss == 59) {
                        sytyta(5);
                    } else if (ss == 0) {
                        tapahtuma.lahtoaika = System.currentTimeMillis();
                        tapahtuma.lahtoaika -= tapahtuma.lahtoaika % 100;
                        if (!tapahtumalist.contains(tapahtuma)) tapahtumalist.add(0,tapahtuma);
                        tapahtumatadapter.notifyDataSetChanged();

                        sytyta(0);
                        tila = 2;
                        naytaLahtobutton();
                    }

                    if (iVali == 30) {
                        if (ss == 25) {
                            sytyta(1);
                        } else if (ss == 26) {
                            sytyta(2);
                        } else if (ss == 27) {
                            sytyta(3);
                        } else if (ss == 28) {
                            sytyta(4);
                        } else if (ss == 29) {
                            sytyta(5);
                        } else if (ss == 30) {
                            tapahtuma.lahtoaika = System.currentTimeMillis();
                            tapahtuma.lahtoaika -= tapahtuma.lahtoaika % 100;
                            if (!tapahtumalist.contains(tapahtuma)) tapahtumalist.add(0,tapahtuma);
                            tapahtumatadapter.notifyDataSetChanged();

                            sytyta(0);
                            tila = 2;
                            naytaLahtobutton();
                        }
                    }
                }
                int mil = now.get(Calendar.MILLISECOND);

                timeHandler.postDelayed(this, 1000 - mil);
            }
        };

        gFilename = getFilenameToday("latest");
        LataaFile (gFilename);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ValikkoActivity.class);

                intent.putExtra("VALI", iVali);
                startActivityForResult(intent,VALI_REQUEST);

            }
        });

        AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG,"longclick i:" + i);
                KysyKeraaja(i);
                return false;
            }
        };
        lTapahtumat.setOnItemLongClickListener(longClickListener);
    }

    private void KysyKeraaja(int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        final Tapahtuma tap = tapahtumalist.get(pos);
        String msg = tap.msg;

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.kysymsg, null);
        final EditText etMsg = (EditText) view.findViewById(R.id.etMsg);

        etMsg.setText(msg);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.e(TAG,"Msg:" + etMsg.getText().toString());
                        tap.msg = etMsg.getText().toString();
                        tallennaKaikkiTapahtumat();

                    }
                })
                .setNegativeButton("Peruuta", null)
                .setTitle("Teksti");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Palautetaan tämän päivän viimeisin sessio, jos on. Jos ei niin perusmuotoinen
    private String getFilenameToday(String sAction) {
        // action next, lastest
        String sRet = "", sFilename;
        SimpleDateFormat filedateFormat = new SimpleDateFormat(
                "yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        sFilename = gFilebase + "_" + filedateFormat.format(cal.getTime()) + ".txt";
        File path = new File(Environment.getExternalStorageDirectory().toString() + "/AAA");
        File file = new File(path, sFilename);
        sRet = sFilename;

        int i = 1;
        while (file.exists()) {
            if (sAction.equals("latest")) sRet = sFilename;
            // tutkitaan onko tehty myöhäisempiä
            sFilename = gFilebase + "_" + filedateFormat.format(cal.getTime()) + "_" + String.valueOf(i) +  ".txt";
            file = new File (path, sFilename);
            if (sAction.equals("next")) sRet = sFilename;
            i++;
        }
        return sRet;
    }

    private void LataaFile (String filename) {
        // Luetaan tiedoston tapahtumat listalle, jos löytyy
        Log.e(TAG,"lataaFile:" + filename);
        tapahtumalist.clear();
        try {
            File path = new File(Environment.getExternalStorageDirectory().toString() + "/AAA");
            File file = new File(path,gFilename);

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            Tapahtuma tap;
            while ((line = br.readLine()) != null) {
                try {
                    tap = Tapahtuma.parse(line);
                    if (tap.lahtoaika > 0)
                        tapahtumalist.add(0, tap);
                }catch (Exception ex) {
                    Log.e(TAG,"ex:" + ex.getMessage());
                }
            }
            br.close();
            //tapahtumatadapter.notifyDataSetChanged();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            tapahtumatadapter.notifyDataSetChanged();

        }
    }

    private void PiirraVali() {
        if (c != null) {
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            String sx = String.valueOf(iVali);
            c.drawColor(0, PorterDuff.Mode.CLEAR);
            c.drawText(sx, 0, height - 5, paint);
        }
    }

    private void sytyta(int lkm) {

        if (lkm == 1) {
            img1.setBackgroundColor(Color.RED);
        } else if (lkm == 2) {
            img1.setBackgroundColor(Color.RED);
            img2.setBackgroundColor(Color.RED);
        } else if (lkm == 3) {
            img1.setBackgroundColor(Color.RED);
            img2.setBackgroundColor(Color.RED);
            img3.setBackgroundColor(Color.RED);
        } else if (lkm == 4) {
            img1.setBackgroundColor(Color.RED);
            img2.setBackgroundColor(Color.RED);
            img3.setBackgroundColor(Color.RED);
            img4.setBackgroundColor(Color.RED);
        } else if (lkm == 5) {
            img1.setBackgroundColor(Color.RED);
            img2.setBackgroundColor(Color.RED);
            img3.setBackgroundColor(Color.RED);
            img4.setBackgroundColor(Color.RED);
            img5.setBackgroundColor(Color.RED);
        } else if (lkm == 0) {
            img1.setBackgroundColor(Color.WHITE);
            img2.setBackgroundColor(Color.WHITE);
            img3.setBackgroundColor(Color.WHITE);
            img4.setBackgroundColor(Color.WHITE);
            img5.setBackgroundColor(Color.WHITE);

        }


    }

    private void PiilotaBaarit() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.hide();
    }

    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //finish();
                        //System.exit(0);
                    }
                });
                builder.show();
            }
        } catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE." + e.getMessage());
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //finish();
                    //System.exit(0);
                }

            });
            builder.show();

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        timeHandler.removeCallbacks(updateTimeTask);

        super.onPause();
        beaconManager.removeRangeNotifier(rangeNotifier);
        beaconManager.unbind(this);
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        beaconManager.bind(this);
        timeHandler.removeCallbacks(updateTimeTask);
        timeHandler.post(updateTimeTask);
        application = ((BeaconReferenceApplication) this.getApplicationContext());
        application.disableMonitoring();

        sytyta(0);
    }

    private void naytaLahtobutton() {

        if (tila == 1 || tila == 2) {
            btnLahtoon.setBackgroundColor(Color.GREEN);
            btnLahtoon.setText("KESKEYTÄ");
        } else {
            btnLahtoon.setBackgroundColor(Color.GRAY);
            btnLahtoon.setText("LÄHTÖÖN");
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String sAction = "";
        // Check which request we're responding to
        if (requestCode == VALI_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                sAction = data.getStringExtra("ACTION");
                Log.e(TAG,"sAction:" + sAction);
                gFilename = getFilenameToday("next");
                LataaFile(gFilename);

            }
        }
    }

    public void onClickLahtoon (View v) {
        if (tila == 0) tila = 1;
        else if (tila == 1 || tila == 2) tila = 0;

        naytaLahtobutton();
        if (tila == 1) {
            tapahtuma = new Tapahtuma();

            application.enableMonitoring();
        }
        else if (tila == 0) {
            tapahtuma = null;
            application.disableMonitoring();
            sytyta(0);
        }
    }
    RangeNotifier rangeNotifier = new RangeNotifier() {

        public List<Beacon> beaconList = new ArrayList<Beacon>();

        // näitä tulee sekunnin välein
        @Override
        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
            List<Beacon> tmpbeaconList = new ArrayList<Beacon>(beacons);
            OmaBeacon tmpOma;
            if (beacons.size() > 0) {
                Beacon tmpBeacon;
                //Log.e(TAG,"didRange count:" + tmpbeaconList.size());
                // Käydään saatu lista läpi
                // lisätään listalle uudet
                // lisätään myös omalle listalle
                // päivitetään lastSeen tietoa oman listan omabeaconeille
                boolean onjo = false;
                long aika1 = 0, aika2 = 0;
                while (beacons.iterator().hasNext()) {
                    tmpBeacon = beacons.iterator().next();

                    // Hylätään tuntemattomat
                    if (!(tmpBeacon.getId3().toString().equals("4660") || tmpBeacon.getId3().toString().equals("4661"))) {
                        beacons.remove(tmpBeacon);
                        continue;
                    }
                    if (application.isMonitoringOn()) {
                        aika1 = application.viimeisinAika1;
                        aika2 = application.viimeisinAika2;

                        // Vähennetään ohikulkuaika ja laitetaan tapahtumalle
                        if (tmpBeacon.getId3().toString().equals("4660")) {
                            aika1 -= tmpBeacon.getId2().toInt();
                            Log.e(TAG,"Vähennetiin 4660:" + tmpBeacon.getId2());
                            if (tapahtuma.ekaaika == 0) {
                                tapahtuma.ekaaika = aika1;
                                if (!tapahtumalist.contains(tapahtuma))
                                    tapahtumalist.add(0,tapahtuma);
                                tapahtumatadapter.notifyDataSetChanged();

                            }
                        }
                        if (tmpBeacon.getId3().toString().equals("4661")) {
                          //  if (tapahtuma.ekaaika == 0)continue;
                            aika2 -= tmpBeacon.getId2().toInt();
                            Log.e(TAG,"Vähennetiin 4661:" + tmpBeacon.getId2());
                            if (tapahtuma.tokaaika == 0) {
                                tapahtuma.tokaaika = aika2;
                                application.disableMonitoring();
                                if (tapahtuma.lahtoaika == 0) {
                                    if (tapahtumalist.contains(tapahtuma))
                                        tapahtumalist.remove(tapahtuma);
                                }
                                else if (!tapahtumalist.contains(tapahtuma))
                                    tapahtumalist.add(0, tapahtuma);
                                tapahtumatadapter.notifyDataSetChanged();
                                tallennaTapahtuma(tapahtuma,gFilename);
                                tila = 0;
                                naytaLahtobutton();
                            }
                        }

                        Log.e(TAG,"dide aika id3:" + tmpBeacon.getId3() + " " + BeaconReferenceApplication.getDate(aika1,"HH:mm:ss.SSS")
                            + " " + BeaconReferenceApplication.getDate(aika2,"HH:mm:ss.SSS")
                            + " " + tapahtuma.toString("kello"));
                    }


                    // Tunnistuuko samaksi kaikki neljä lähetystä??
                    if (beaconList.contains(tmpBeacon)) {
                        Log.e(TAG,"oli jo listalla:" + tmpBeacon.getId3() + " " + tmpBeacon.getId2());
                        onjo = true;
                    }
                    for (Beacon tmp: beaconList) {
                        if (tmp.getId3().equals(tmpBeacon.getId3())) {
                            onjo = true;
                            break;
                        }
                    }

                    if (!onjo) {
                        Log.e(TAG,"Lisätään:" + tmpBeacon.getId3() + " " + tmpBeacon.getId2().toInt());
                        beaconList.add(tmpBeacon);
                        tmpOma = new OmaBeacon(tmpBeacon,System.currentTimeMillis());
                        omabeaconList.add(tmpOma);
                    }
                    else {
                        for (int indexOmat = omabeaconList.size() - 1; indexOmat >= 0; indexOmat--) {
                            OmaBeacon tmp = omabeaconList.get(indexOmat);
                            if (tmp.getId3().equals(tmpBeacon.getId3())) {
                                // Poistetaan edellinen ja laitetaan uusi
                                // ei pysty päivittämään id kenttiä
                                omabeaconList.remove(tmp);
                                tmpOma = new OmaBeacon(tmpBeacon,System.currentTimeMillis());
                                omabeaconList.add(tmpOma);
                            }
                        }
                    }
                    beacons.remove(tmpBeacon);
                }
            } // if size > 0

            // Poistetaan omalta listalta jos ei ole näkynyt vähään aikaan
            //
            for (int indexOmat = omabeaconList.size() - 1; indexOmat >= 0; indexOmat--) {
                OmaBeacon tmp = omabeaconList.get(indexOmat);
                if (tmp.getLastSeen() < (System.currentTimeMillis() - 6000000)) {
                    Log.e(TAG,"Poistetaan:" + tmp.getId3() + " Kummaltakin listalta");
                    for (int indexKaikki = beaconList.size() - 1; indexKaikki >= 0; indexKaikki--)  {
                        Beacon tmpBeacon = beaconList.get(indexKaikki);
                        if (tmpBeacon.getId3().equals(tmp.getId3())) {
                            beaconList.remove(indexKaikki);
                        }
                    }
                    omabeaconList.remove(indexOmat);
                }
            }
//            for (OmaBeacon tmp: omabeaconList) {
//                String sX = tmp.getId3() + " id2:" + tmp.getId2().toString()
//                        + " id1:" + tmp.getId1().toString()
//                        + " rssi:" + tmp.getRssi()
//                        + " cnt:" + tmp.getPacketCount()
//                        + " Nähty ms:" + (System.currentTimeMillis() - tmp.getLastSeen());
//                Log.e(TAG,sX);
//               // logToDisplay(sX);
//            }
            omabeaconadapter.notifyDataSetChanged();
        } // didBeacons
    };
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Lopetetaan?")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Ranging2Activity.this.finish();
                    }
                })
                .setNegativeButton("Ei", null)
                .show();
    }
    @Override
    public void onBeaconServiceConnect() {

        Log.e(TAG,"onBeaconServiceConnect");
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
            beaconManager.addRangeNotifier(rangeNotifier);
        } catch (RemoteException e) {
            Log.e(TAG, "catch onBeaconServiceConnect " + e.getMessage());
        }

    }

    private void tallennaKaikkiTapahtumat() {
        write2File(gFilename,"",false);
        for (int indexOmat = tapahtumalist.size() - 1; indexOmat >= 0; indexOmat--) {
            Tapahtuma tap = tapahtumalist.get(indexOmat);
            tallennaTapahtuma(tap, gFilename);
        }
    }
    private void tallennaTapahtuma (Tapahtuma tapahtuma, String filename) {
        String viesti = tapahtuma.toString("tallennus");
        Log.e(TAG,"viesti:" + viesti);
        write2File(filename ,viesti,true);
    }
    public void write2File (String filename, String msg, boolean append) {

        SimpleDateFormat filedateFormat = new SimpleDateFormat(
                "yyyy-MM-dd");

        //Calendar cal = Calendar.getInstance();
        //filename += "_" + filedateFormat.format(cal.getTime()) + ".txt";

        File path = new File(Environment.getExternalStorageDirectory().toString() + "/AAA");
        File file = new File(path,filename);

        // Make sure the Pictures directory exists.
        try {
            path.mkdirs();
            if (!file.exists())
            {
                file.createNewFile();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "ERROR:" + e.getMessage());
        }

        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, append));
            buf.append(msg);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG, "Write ERROR:" + e.getMessage());
        }
    }
}
