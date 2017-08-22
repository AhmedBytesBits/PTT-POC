package com.taqneen.tamamcars.poc.ptt.ptt_poc;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    public VoiceLayerClientWorker vlc_worker;

    //    private static final String TAG = MainActivity.class.getSimpleName();
//    private Button ptt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button ptt = (Button)findViewById(R.id.PTT_Button);
        assert ptt != null;
        ptt.setEnabled(false);

        Spinner channels_list = (Spinner)findViewById(R.id.channels_list);
        ArrayAdapter<CharSequence> channels_adapter = ArrayAdapter.createFromResource(this,
                R.array.channels_labels, android.R.layout.simple_spinner_dropdown_item);

        assert channels_list != null;
        channels_list.setAdapter(channels_adapter);
        channels_list.setOnItemSelectedListener(new ChannelsListActivity(this));

        final MediaPlayer apush = MediaPlayer.create(this, R.raw.ptt_push);
        final MediaPlayer rpush = MediaPlayer.create(this, R.raw.ptt_release);
        final String userid = "ahmedrshdy";
        final VoiceLayerClientAuthenticate vlc_auth = new VoiceLayerClientAuthenticate(this);




        try{
            vlc_auth.auth(userid, new AuthCompleted());
        } catch (Exception e) {
            e.printStackTrace();
        }

        ptt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction()  == MotionEvent.ACTION_DOWN){
                    apush.start();
                } else if (event.getAction() == MotionEvent.ACTION_UP){
                    rpush.start();
                }

                return false;
            }
        });
        }

    public void initilaizeVLC(Context context, String token){
        vlc_worker = new VoiceLayerClientWorker(context, token);
    }

    void setChannel(String channel){
        vlc_worker.setChannel(channel);
        Log.d("vlc", "channel selcted: "+ vlc_worker.getChannel());
    };

}

class AuthCompleted extends Activity implements OnAuthCompleteListener{
    private MainActivity activity = new MainActivity();

    @Override
    public void doSomeThing(final Context context, final String token) {
        final Button ptt_btn = (Button)((Activity) context).findViewById(R.id.PTT_Button);
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ptt_btn.setEnabled(true);
                activity.initilaizeVLC(context, token);
//                MainActivity.vlc_worker = new VoiceLayerClientWorker(context, token);

            }
        });
        Log.d("VLC", "Used token: "+ token);
    }
}

class ChannelsListActivity implements AdapterView.OnItemSelectedListener {
    private Context context;
    private MainActivity activity;

    ChannelsListActivity(MainActivity _context){
        context = _context;
        activity = (MainActivity)_context;
    }
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        Object z = parent.getItemAtPosition(pos);
        final String value = ((Activity)context).getResources().getStringArray(R.array.channels_values)[pos];
//        activity.setChannel(value);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setChannel(value);
            }
        });
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}