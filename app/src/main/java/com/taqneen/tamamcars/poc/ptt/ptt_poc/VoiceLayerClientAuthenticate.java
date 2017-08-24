package com.taqneen.tamamcars.poc.ptt.ptt_poc;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Random;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.crypto.MacProvider;

/**
 * Created by ahmedrshdy on 8/16/17.
 */

class VoiceLayerClientAuthenticate {

    private final OkHttpClient client = new OkHttpClient();
    private String jwt;
    private String nonce;
    private String username;
    private Jws<Claims> claims;
    private OnAuthCompleteListener authlistener;
    private static final String apisecret  = "ff32e8ffed89f06aab6f65b98c065661";
    private static final String apikey  = "2cec1bac817c3739265940add7e46b27";
    private Context context;
    private final String key = Base64.encodeToString(apisecret.getBytes(), Base64.DEFAULT);

    VoiceLayerClientAuthenticate(Context _context){
        context = _context;
    }
    protected String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
    void auth(String userid, OnAuthCompleteListener callback) throws Exception{
        try{
            authlistener = callback;
            username = userid;

            getNonce(getSaltString());

        } catch (Exception e){
            Log.d("VLC","failed connection");
            e.printStackTrace();
        }
    }


    private void getNonce(String userid){

        jwt = Jwts.builder()
                .setIssuedAt(new java.util.Date())
                .setIssuer(apikey)
                .setId(userid)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
        try {

            claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwt);

            Log.d("VLC", "JWT creation successful");

        } catch (SignatureException e) {

            Log.e("VLC", "JWT creation failed");
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObject json = new JsonObject();
        json.addProperty("iat",String.valueOf(claims.getBody().getIssuedAt().getTime()/1000));
        json.addProperty("jti", claims.getBody().getId());
        json.addProperty("iss", claims.getBody().getIssuer());
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.toString());

        Request request = new Request.Builder()
                .url("https://apiv3.voicelayer.io/v1/users/auth/nonce")
                .header("Content-Type", "application/json")
                .header("charset", "utf-8")
                .header("Authorization", "Bearer " + jwt)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("VLC","failed connection");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {

                if(response.code() !=400) {
                    final Gson gson = new Gson();
                    NonceResponse g = gson.fromJson(response.body().string(), NonceResponse.class);
                    nonce = g.nonce;
                    Log.d("VLC", "successfully gotten nonce");
                    getToken();

                }else{
                    Log.e("VLC", "failed gotten nonce");
                    Log.e("VLC", response.message());

                }



            }
        });
    }

    private void getToken()
    {

        jwt = Jwts.builder()
                .setIssuedAt(new java.util.Date())
                .setIssuer(apikey)
                .setId(nonce)
                .setSubject(username)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
        try {

            claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwt);

            Log.d("VLC", "JWT creation successful");

        } catch (SignatureException e) {

            Log.e("VLC", "JWT creation failed");
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObject json = new JsonObject();
        json.addProperty("iat",String.valueOf(claims.getBody().getIssuedAt().getTime()/1000));
        json.addProperty("jti", nonce);
        json.addProperty("iss", apikey);
        json.addProperty("sub", username);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.toString());

        Request request = new Request.Builder()
                .url("https://apiv3.voicelayer.io/v1/users/auth/token")
                .header("Content-Type", "application/json")
                .header("charset", "utf-8")
                .header("Authorization", "Bearer " + jwt)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("VLC","failed connection");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {

                if (response.code() != 400) {
                    final Gson gson = new Gson();
                    TokenResponse r = gson.fromJson(response.body().string(), TokenResponse.class);

                    Log.d("VLC", "successfully gotten token");
                    authlistener.doSomeThing(context, r.token);
                } else {
                    Log.e("VLC", "failed gotten nonce");
                    Log.e("VLC", response.message());
                }

            }
        });
    }

}

interface OnAuthCompleteListener{
    public void doSomeThing(Context context, String token);
}
