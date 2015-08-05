package com.not_insane.housecontroller2;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

public class MainActivity extends AppCompatActivity {

    View tempView = null, colorView = null;
    SeekBar tempSeek = null, colorSeek = null, brightnessSeek = null;
    Switch onoff = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tempView = (View)findViewById(R.id.temp);
        colorView = (View)findViewById(R.id.col);

        tempSeek = (SeekBar) findViewById(R.id.seekBar2);
        colorSeek = (SeekBar) findViewById(R.id.seekBar3);
        brightnessSeek = (SeekBar) findViewById(R.id.seekBar);

        onoff = (Switch) findViewById(R.id.switch1);

        onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                comm("1/set/status/" + (isChecked ? 1 : 0), null);
            }
        });

        brightnessSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //System.out.println(progress);
                if(progress % 5 == 0)
                    comm("1/set/brightness/" + (progress), null);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                comm("1/set/brightness/" + (seekBar.getProgress()), null);
            }
        });


        colorSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //System.out.println(progress);
                int val = (int) ((progress / 100.0) * 255);
                if(val % 5 == 0)
                    comm("1/set/color/" + (val), null);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int val = (int) ((seekBar.getProgress() / 100.0) * 255);
                comm("1/set/color/" + (val), null);
            }
        });

        tempSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //System.out.println(progress);
                int val = (int) ((progress / 100.0) * 255);
                if(val % 5 == 0)
                    comm("1/set/temp/" + (val), null);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int val = (int) ((seekBar.getProgress() / 100.0) * 255);
                comm("1/set/temp/" + (val), null);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh stats
        comm("1/get/status/", new Handler() {
            @Override
            public void handle(String result) {
                result = result.trim();
                if(result.equals("1")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onoff.setChecked(true);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onoff.setChecked(false);
                        }
                    });
                }
            }
        });
        comm("1/get/brightness/", new Handler() {
            @Override
            public void handle(String result) {
                result = result.trim();
                brightnessSeek.setProgress(Integer.parseInt(result));
            }
        });
        comm("1/get/color/", new Handler() {
            @Override
            public void handle(String result) {
                result = result.trim();
                colorSeek.setProgress((int) ((Integer.parseInt(result) / 255.0) * 100));
            }
        });
        comm("1/get/temp/", new Handler() {
            @Override
            public void handle(String result) {
                result = result.trim();
                tempSeek.setProgress((int) ((Integer.parseInt(result) / 255.0)  * 100));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void comm(final String module, final Handler h) {
        AsyncTask<Void, Void, Void> execute = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                BufferedReader in = null;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet();
                    request.setURI(new URI("http://192.168.2.224:8090/" + module));
                    HttpResponse response = client.execute(request);
                    in = new BufferedReader
                            (new InputStreamReader(response.getEntity().getContent()));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";
                    String NL = System.getProperty("line.separator");
                    while ((line = in.readLine()) != null) {
                        sb.append(line + NL);
                    }
                    in.close();
                    String page = sb.toString();
                    System.out.println(page);
                    if (h != null) h.handle(page);
                } catch (Exception ee) {
                    ee.printStackTrace();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }
        }.execute((Void) null);
    }
    public abstract class Handler {
        public abstract void handle(String result);
    }
}
