package com.taqneen.tamamcars.poc.ptt.ptt_poc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static VoiceLayerClientWorker vlc_worker;
    private static String currentchannel;
    private static String userid = null;
    private static MediaPlayer sout;


    //    private static final String TAG = MainActivity.class.getSimpleName();
//    private Button ptt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final VoiceLayerClientAuthenticate vlc_auth = new VoiceLayerClientAuthenticate(this);
        final MediaPlayer apush = MediaPlayer.create(this, R.raw.ptt_push);
        final MediaPlayer rpush = MediaPlayer.create(this, R.raw.ptt_release);
        sout = MediaPlayer.create(this, R.raw.ptt_out);

        Button ptt = (Button)findViewById(R.id.PTT_Button);
        Button connect = (Button)findViewById(R.id.Connect_Button);
        Spinner channels_list = (Spinner)findViewById(R.id.channels_list);
        final EditText username = (EditText)findViewById(R.id.username);

        assert ptt != null;
        assert connect != null;
        assert username != null;

        ptt.setEnabled(false);
        connect.setEnabled(false);

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userid = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    vlc_auth.auth(userid, new AuthCompleted());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        ArrayAdapter<CharSequence> channels_adapter = ArrayAdapter.createFromResource(this,
                R.array.channels_labels, android.R.layout.simple_spinner_dropdown_item);

        assert channels_list != null;
        channels_list.setAdapter(channels_adapter);
        channels_list.setOnItemSelectedListener(new ChannelsListActivity(this));


        ptt.setOnTouchListener(new View.OnTouchListener() {
            static final int REQUEST_MICROPHONE = 1 ;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction()  == MotionEvent.ACTION_DOWN){
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                REQUEST_MICROPHONE);

                    }
                    apush.start();
                    vlc_worker.startRecording();

                } else if (event.getAction() == MotionEvent.ACTION_UP){
                    rpush.start();
                    float r = vlc_worker.endRecording();

                }

                return false;
            }
        });
        }


    public void initilaizeVLC(Context context, String token){
//        TODO: send listenTochennel() to callback
        vlc_worker = new VoiceLayerClientWorker(context, token);
        vlc_worker.setChannel(currentchannel);
        vlc_worker.listenToChannel(new ListenCompleted());
    }

    public void setCurrentChannel(String v){
        currentchannel = v;

        if (userid != null && !currentchannel.equals("")){
            Button connect = (Button)findViewById(R.id.Connect_Button);
            connect.setEnabled(true);

        }

    }

    public void playOut(){
        sout.start();
    }

}

class AuthCompleted implements OnAuthCompleteListener{
    private MainActivity activity = new MainActivity();

    @Override
    public void doSomeThing(final Context context, final String token) {
        activity.initilaizeVLC(context, token);
        Log.d("VLC", "Used token: "+ token);
    }
}

class ChannelsListActivity implements AdapterView.OnItemSelectedListener {
    private Context context;
    private MainActivity activity;
    private VoiceLayerClientWorker vworker;

    ChannelsListActivity(Context _context){
        context = _context;
    }
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        Object z = parent.getItemAtPosition(pos);
        final String value = ((Activity)context).getResources().getStringArray(R.array.channels_values)[pos];
        ((MainActivity)context).setCurrentChannel(value);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}

class ListenCompleted implements OnListenToChannel{

    @Override
    public void onSuccessLiesten(Context context) {
        final Button ptt_btn = (Button)((Activity) context).findViewById(R.id.PTT_Button);
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ptt_btn.setEnabled(true);

            }
        });
    }
}