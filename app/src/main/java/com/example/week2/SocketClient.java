package com.example.week2;

import android.app.Application;
import android.util.Log;

import com.google.gson.JsonParser;
import com.kakao.sdk.common.KakaoSdk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketClient extends Application {

    private static volatile SocketClient instance = null;
    private Socket mSocket;
    private User user;
    private Pokemon pokemon;

    ArrayList<String> pokemon_list = new ArrayList<>(Arrays.asList(
            "모부기", "수풀부기", "토대부기", "불꽃숭이", "파이숭이", "초염몽", "팽도리", "팽태자", "엠페르트"
    ));

    public static SocketClient getGlobalApplicationContext() {
        if(instance == null) {
            throw new IllegalStateException("this application does not inherit com.kakao.GlobalApplication");
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        KakaoSdk.init(this, getString(R.string.kakaoApi));


        try {
            mSocket = IO.socket(getString(R.string.serverAddr));
        } catch (URISyntaxException e) {
            Log.e("socketIO", e.toString());
        }

        mSocket.on("userInfo", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];

                try {
                    user.setCoin(data.getLong("coin"));
                    pokemon = parsePokemon(data.getJSONObject("pokemon"));
                    user.setPoke(pokemon);
                    user.setName(data.getString("name"));
                    Log.i("socketIO", pokemon.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        mSocket.connect();
    }

    private Pokemon parsePokemon(JSONObject obj) throws JSONException {
        int number = obj.getInt("number");
        ArrayList<Skill> skills = new ArrayList<>();
        JSONArray jsonArray = obj.getJSONArray("skills");
        for(int i=0;i<jsonArray.length();i++){
            JSONObject skill = jsonArray.getJSONObject(i);
            skills.add(new Skill(skill.getInt("id"), skill.getString("name"),
                    skill.getDouble("cool"), skill.getInt("level"), skill.getInt("power")));
        }

        return new Pokemon(obj.getInt("id"), obj.getInt("level"),
                number, pokemon_list.get(number), obj.getDouble("exp"), skills);
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public Pokemon getPokemon() {
        return pokemon;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
        mSocket.disconnect();
    }

    public void requestUserInfo(String kakaoId){
        try {
            user = new User(kakaoId, null, null, 0);
            mSocket.emit("userInfo", new JSONObject("{\"user_id\":\""+kakaoId+"\"}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void notifySkillChange(){
        Log.i("socket", user.toString());

    }

}