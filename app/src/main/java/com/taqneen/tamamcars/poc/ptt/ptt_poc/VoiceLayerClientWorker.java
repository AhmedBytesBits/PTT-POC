package com.taqneen.tamamcars.poc.ptt.ptt_poc;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.net.MalformedURLException;
import java.util.EnumSet;
import java.util.List;

import io.voicelayer.voicelayerSdk.PaginatedResponse;
import io.voicelayer.voicelayerSdk.VoiceLayerChannel;
import io.voicelayer.voicelayerSdk.VoiceLayerClient;
import io.voicelayer.voicelayerSdk.VoiceLayerConfiguration;
import io.voicelayer.voicelayerSdk.VoiceLayerMessage;
import io.voicelayer.voicelayerSdk.VoiceLayerMessageRecorder;
import io.voicelayer.voicelayerSdk.VoiceLayerRecorderEvent;
import io.voicelayer.voicelayerSdk.VoiceLayerUser;
import io.voicelayer.voicelayerSdk.exceptions.VoiceLayerException;
import io.voicelayer.voicelayerSdk.exceptions.VoiceLayerRecorderException;
import io.voicelayer.voicelayerSdk.interfaces.VoiceLayerChannelSubscriptionCallback;
import io.voicelayer.voicelayerSdk.interfaces.VoiceLayerCreateCallback;
import io.voicelayer.voicelayerSdk.interfaces.VoiceLayerFetchCallback;
import io.voicelayer.voicelayerSdk.interfaces.VoiceLayerJoinChannelCallback;
import io.voicelayer.voicelayerSdk.interfaces.VoiceLayerLoginCallback;
import io.voicelayer.voicelayerSdk.interfaces.VoiceLayerRecorderEventListener;
import io.voicelayer.voicelayerSdk.interfaces.VoiceLayerRemoveCallback;

import static io.jsonwebtoken.lang.Collections.size;

/**
 * Created by ahmedrshdy on 8/18/17.
 */

public class VoiceLayerClientWorker{
    private String channel;
    private String username;

    private VoiceLayerUser CURRENT_USER;
    private VoiceLayerChannel CURRENT_CHANNEL;
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

    void listenToChannel(){
        vlc.getPublicChannels(1, 5, new VoiceLayerFetchCallback<PaginatedResponse<List<VoiceLayerChannel>>>() {
                @Override
                public void onFetchComplete(PaginatedResponse<List<VoiceLayerChannel>> listPaginatedResponse, VoiceLayerException e1) {
                    if(size(listPaginatedResponse.getResult())==0) {
                        createChannel();
                    }

                    for(int i=0; i<size(listPaginatedResponse.getResult()); i++){
                        if(listPaginatedResponse.getResult().get(i).name.equals( channel)){
                            Log.d("vlc", "channel is already present");
                            CURRENT_CHANNEL = listPaginatedResponse.getResult().get(i);
                            joinChannel();
                        }
                    }
                }

            });
    }

    private void createChannel(){
        EnumSet<VoiceLayerChannel.VoiceLayerChannelTrait> traits = EnumSet.of(
                VoiceLayerChannel.VoiceLayerChannelTrait.PUBLIC // Mark the channel as pulic
        );

        vlc.createChannel(channel, traits, new VoiceLayerCreateCallback<VoiceLayerChannel>() {
            @Override
            public void onCreateComplete(VoiceLayerChannel channel, VoiceLayerException exception) {
                CURRENT_CHANNEL = channel;
                Log.d("vlc", "channel created with name:" + channel.name);
                listenToChannel();
            }
        });

    }

    private void joinChannel(){
        CURRENT_CHANNEL.join(new VoiceLayerJoinChannelCallback() {

            @Override
            public void onJoinChannelComplete(VoiceLayerException e) {
                Log.d("vlc", "channel joined: " + channel);
                subChannel();

            }

        });

    }

    private void subChannel(){
        CURRENT_CHANNEL.subscribe(new VoiceLayerChannelSubscriptionCallback() {
            @Override
            public void onSubscribedToChannelEvents(VoiceLayerChannel voiceLayerChannel, VoiceLayerException e) {
                Log.d("vlc", "subscribed into channel: " + channel);

            }

            @Override
            public void onUnsubscribeFromChannelEvents(VoiceLayerChannel voiceLayerChannel, VoiceLayerException e) {

            }
        });
    }

    void startRecording() {
        final VoiceLayerMessageRecorder recorder = vlc.getMessageRecorder();

        recorder.setRecorderEventListener(new VoiceLayerRecorderEventListener() {

            @Override
            public void onRecorderEvent(VoiceLayerRecorderEvent event, VoiceLayerMessage message) {
                // Called when recording starts and finishes.
                Log.d("vlc", "recording success");
            }

            @Override
            public void onRecordingFailed(VoiceLayerRecorderException e, VoiceLayerMessage voiceLayerMessage) {
                Log.d("vlc", "recording failed");

            }

            @Override
            public void onUploadFailed(VoiceLayerRecorderException e, VoiceLayerMessage voiceLayerMessage) {
                Log.d("vlc", "uploading failed");

            }
        });
        VoiceLayerMessage vmsg = recorder.startRecording(CURRENT_CHANNEL);

        boolean s = recorder.isRecording();
        Log.d("vlc", "is recording: " + s);

    }

    float endRecording(){
        float msg_length;
        final VoiceLayerMessageRecorder recorder = vlc.getMessageRecorder();
        msg_length = recorder.stopRecording();
    //        recorder.setRecorderEventListener(null);
            return msg_length;
        }

}
