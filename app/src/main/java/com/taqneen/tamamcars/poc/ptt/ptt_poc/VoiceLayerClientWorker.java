package com.taqneen.tamamcars.poc.ptt.ptt_poc;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.net.MalformedURLException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import io.voicelayer.voicelayerSdk.PaginatedResponse;
import io.voicelayer.voicelayerSdk.VoiceLayerChannel;
import io.voicelayer.voicelayerSdk.VoiceLayerChannelInvitation;
import io.voicelayer.voicelayerSdk.VoiceLayerClient;
import io.voicelayer.voicelayerSdk.VoiceLayerConfiguration;
import io.voicelayer.voicelayerSdk.VoiceLayerMessage;
import io.voicelayer.voicelayerSdk.VoiceLayerMessagePlayer;
import io.voicelayer.voicelayerSdk.VoiceLayerMessageRecorder;
import io.voicelayer.voicelayerSdk.VoiceLayerRecorderEvent;
import io.voicelayer.voicelayerSdk.VoiceLayerUser;
import io.voicelayer.voicelayerSdk.exceptions.VoiceLayerException;
import io.voicelayer.voicelayerSdk.exceptions.VoiceLayerRecorderException;
import io.voicelayer.voicelayerSdk.interfaces.VoiceLayerChannelRequestCallback;
import io.voicelayer.voicelayerSdk.interfaces.VoiceLayerChannelSubscriptionCallback;
import io.voicelayer.voicelayerSdk.interfaces.VoiceLayerCreateCallback;
import io.voicelayer.voicelayerSdk.interfaces.VoiceLayerFetchCallback;
import io.voicelayer.voicelayerSdk.interfaces.VoiceLayerLoginCallback;
import io.voicelayer.voicelayerSdk.interfaces.VoiceLayerMessageEventListener;
import io.voicelayer.voicelayerSdk.interfaces.VoiceLayerRecorderEventListener;

import static io.jsonwebtoken.lang.Collections.size;

/**
 * Created by ahmedrshdy on 8/18/17.
 */

public class VoiceLayerClientWorker{
    private String channel;

    private VoiceLayerUser currentUser;
    private VoiceLayerChannel currentChannel;
    private OnListenToChannel listenCompleted;
    private VoiceLayerClient vlc;
    Queue<VoiceLayerMessage> messages=new LinkedList<VoiceLayerMessage>();
    VoiceLayerMessagePlayer player;



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
                    currentUser = voiceLayerUser;
                    Log.d("vlc", "user id: " + currentUser.id);
                }
            });
        } catch (Exception e){
            Log.e("vlc", "log in failed");
            e.printStackTrace();
        }

        final VoiceLayerMessageRecorder recorder = vlc.getMessageRecorder();

        recorder.setRecorderEventListener(new VoiceLayerRecorderEventListener() {

            @Override
            public void onRecorderEvent(VoiceLayerRecorderEvent event, VoiceLayerMessage message) {
                // Called when recording starts and finishes.
                if(event.equals(VoiceLayerRecorderEvent.FINISH)){
                    Log.d("vlc", "recording ended");
                }

                if (event.equals(VoiceLayerRecorderEvent.START)){
                    Log.d("vlc", "recording started");

                }

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
//        Handle newly received messages

        vlc.setMessageEventListener(new VoiceLayerMessageEventListener() {
            @Override
            public void onMessagePosted(VoiceLayerMessage voiceLayerMessage) {
                Log.d("vlc", "new message received");
                if(!voiceLayerMessage.user.id.equals(currentUser.id)) {
                    messages.add(voiceLayerMessage);
                    playMessages();
                }
            }

            @Override
            public void onMessageUpdated(VoiceLayerMessage voiceLayerMessage) {

            }

            @Override
            public void onMessageRemoved(String s, VoiceLayerChannel voiceLayerChannel) {

            }

            @Override
            public void onMessageReuploaded(VoiceLayerMessage voiceLayerMessage) {

            }

            @Override
            public void onMissedMessagesRetrieved(Map<VoiceLayerChannel, List<VoiceLayerMessage>> map) {

            }
        });
    }

    void setChannel(String name){
        channel = name;
    }

    void listenToChannel(OnListenToChannel callback){
        listenCompleted = callback;
        vlc.getPublicChannels(1, 5, new VoiceLayerFetchCallback<PaginatedResponse<List<VoiceLayerChannel>>>() {
                @Override
                public void onFetchComplete(PaginatedResponse<List<VoiceLayerChannel>> listPaginatedResponse, VoiceLayerException e1) {
                    if(size(listPaginatedResponse.getResult())==0) {
                        createChannel();
                    }

                    for(int i=0; i<size(listPaginatedResponse.getResult()); i++){
                        if(listPaginatedResponse.getResult().get(i).name.equals( channel)){
                            Log.d("vlc", "channel is already present");
                            currentChannel = listPaginatedResponse.getResult().get(i);
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
                currentChannel = channel;
                Log.d("vlc", "channel created with name:" + channel.name);
                listenToChannel(listenCompleted);
            }
        });

    }

    private void joinChannel(){
        currentChannel.requestToJoin(new VoiceLayerChannelRequestCallback() {

            @Override
            public void onChannelRequestCompleted(VoiceLayerChannelInvitation voiceLayerChannelInvitation, VoiceLayerException e) {
                Log.d("vlc", "channel joined: " + channel);
                subChannel();
            }

        });

    }

    private void subChannel(){
        currentChannel.subscribe(new VoiceLayerChannelSubscriptionCallback() {
            @Override
            public void onSubscribedToChannelEvents(VoiceLayerChannel voiceLayerChannel, VoiceLayerException e) {
                Log.d("vlc", "subscribed into channel: " + channel);
                listenCompleted.onSuccessLiesten(context);
                player = vlc.getMessagePlayer();
            }

            @Override
            public void onUnsubscribeFromChannelEvents(VoiceLayerChannel voiceLayerChannel, VoiceLayerException e) {

            }
        });
    }

    void startRecording() {
        final VoiceLayerMessageRecorder recorder = vlc.getMessageRecorder();
        player.pause();

        VoiceLayerMessage vmsg = recorder.startRecording(currentChannel);
        boolean s = recorder.isRecording();
        Log.d("vlc", " isRcording: " + s);

    }

    float endRecording(){
        float msg_length;
        final VoiceLayerMessageRecorder recorder = vlc.getMessageRecorder();
        msg_length = recorder.stopRecording();
        player.resume();
        Log.d("vlc", "recorded length: " + msg_length);
        return msg_length;
    }

    void playMessages(){

        Iterator it=messages.iterator();

        while(it.hasNext()){
            VoiceLayerMessage message = (VoiceLayerMessage) it.next();
            player.playMessage(message);
            ((MainActivity)context).playOut();
        }
    }
}

interface OnListenToChannel{
    public void onSuccessLiesten(Context context);
}