package com.taqneen.tamamcars.poc.ptt.ptt_poc;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.net.MalformedURLException;
import java.util.EnumSet;

import io.voicelayer.voicelayerSdk.VoiceLayerChannel;
import io.voicelayer.voicelayerSdk.VoiceLayerClient;
import io.voicelayer.voicelayerSdk.VoiceLayerConfiguration;
import io.voicelayer.voicelayerSdk.VoiceLayerUser;
import io.voicelayer.voicelayerSdk.exceptions.VoiceLayerException;
import io.voicelayer.voicelayerSdk.interfaces.VoiceLayerLoginCallback;

/**
 * Created by ahmedrshdy on 8/18/17.
 */

public class VoiceLayerClientWorker{
    private String channel;

    protected VoiceLayerUser CURRENT_USER;
    private VoiceLayerClient vlc;
    private Context context;


    VoiceLayerClientWorker(Context _context, String token){
        context = _context;
        Activity activity = (Activity)context;

        VoiceLayerConfiguration config = new VoiceLayerConfiguration(
                activity.getString(R.string.voicelayer_app_name),
                activity.getString(R.string.android_key),
                activity.getString(R.string.android_secret)
        );

        try {
            VoiceLayerClient.Initialize(context, config);
            vlc = VoiceLayerClient.getInstance();
            Log.d("vlc", "VoicLayerClient initialization success ");


        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e("vlc", "VoicLayerClient initialization failed ");
        }

        try{
            vlc.login(token, new VoiceLayerLoginCallback() {
                @Override
                public void onLoginComplete(VoiceLayerUser voiceLayerUser, String s, VoiceLayerException e) {
                    CURRENT_USER = voiceLayerUser;
                }
            });
        } catch (Exception e){
            Log.e("vlc", "log in failed");
            e.printStackTrace();
        }
    }

    void setChannel(String name){
        channel = name;
    }

    String getChannel(){
        return channel;
    }

    public void createChannel(String name){
        EnumSet<VoiceLayerChannel.VoiceLayerChannelTrait> traits = EnumSet.of(
                VoiceLayerChannel.VoiceLayerChannelTrait.BROADCAST,
                VoiceLayerChannel.VoiceLayerChannelTrait.PUBLIC,
                VoiceLayerChannel.VoiceLayerChannelTrait.MANAGED
        );

    }
}
