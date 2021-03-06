package com.example.week2;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.JsonParser;
import com.kakao.sdk.common.KakaoSdk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketClient extends Application {

    private static volatile SocketClient instance = null;
    private Socket mSocket;
    private User user;
    private Pokemon pokemon;
    private long timeReward;

    public MutableLiveData<JSONArray> raidInfo;
    public MutableLiveData<Integer> raidCnt;
    public MutableLiveData<Integer> bossHp;
    public long addCoin;


    //Map<String,Integer> guild_member = new HashMap<>();
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
        user = new User(null, null, null, 0,0,0,0,3);

        raidInfo = new MutableLiveData<>();
        raidCnt = new MutableLiveData<>();
        bossHp = new MutableLiveData<>();

        try {
            mSocket = IO.socket(getString(R.string.serverAddr));

            mSocket.on("userInfo", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        user.setUser_id(data.getString("user_id"));
                        pokemon = parsePokemon(data.getJSONObject("pokemon"));

                        user.setPoke(pokemon);
                        user.setName(data.getString("name"));
                        user.setGuild(data.getInt("guild"));
                        user.setEndTime(data.getLong("end_time"));

                        user.setRaid_times(data.getInt("raid_cnt"));
                        user.setRaid_damage(data.getInt("raid_damage"));

                        timeReward = (System.currentTimeMillis() - user.getEndTime()) / 1000;
                        addCoin = Math.min(timeReward, 100000);
                        user.setCoin(data.getLong("coin")+addCoin);
                        notifyChange();

                        Log.i("userInfo received", user.toString());



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            mSocket.on("register", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject data = (JSONObject) args[0];
                    Log.i("socketIO", String.valueOf(data));
                    try {
                        user.setUser_id(data.getString("user_id"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            mSocket.on("registerDone", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject data = (JSONObject) args[0];
                    Log.i("thread", data.toString());
                    try {
                        user.setCoin(data.getLong("coin"));
                        pokemon = parsePokemon(data.getJSONObject("pokemon"));
                        user.setPoke(pokemon);
                        user.setName(data.getString("name"));
                        user.setGuild(data.getInt("guild"));
                        user.setRaid_times(data.getInt("raid_cnt"));
                        Log.i("thread2", user.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            mSocket.on("raidInfo", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONArray data = (JSONArray) args[0];
                    Log.i("raidInfo", data.toString());
                    raidInfo.postValue(data);
                }
            });

            mSocket.on("raidCnt", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String data = (String) args[0];
                    //Log.i("raidCnt", data);
                    raidCnt.postValue(Integer.parseInt(data));

                }
            });

            mSocket.on("bossHp", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String data = (String) args[0];
                    Log.i("bossHp", String.valueOf(bossHp.getValue()));
                    if(bossHp != null)
                        bossHp.postValue(Integer.parseInt(data));
                }
            });

        } catch (URISyntaxException e) {
            Log.e("socketIO", e.toString());
        }


        Log.i("socketIO status", String.valueOf(mSocket));




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
                number, pokemon_list.get(number), obj.getLong("exp"), skills);
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
        mSocket.connect();
        try {
            user = new User(null, null, null, 0, 0,0,0,3);
            mSocket.emit("userInfo", new JSONObject("{\"user_id\":\""+kakaoId+"\"}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void register(String query){
        try {
            mSocket.emit("register", new JSONObject(query));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void notifyChange(){
        try {
            user.setEndTime(System.currentTimeMillis());
            JSONObject obj = new JSONObject(user.toString());
            Log.i("socketIO change", obj.toString());

            mSocket.emit("change", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void requestRaidInfo(MutableLiveData<JSONArray> raidInfo, MutableLiveData<Integer> raidCnt){

        mSocket.emit("raid");

    }

    public void requestBossInfo(MutableLiveData<Integer> bossHp){

        try {
            Log.i("boss", "{\"guild\":"+user.getGuild()+"}");
            mSocket.emit("boss", new JSONObject("{\"guild\":"+user.getGuild()+"}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendRaidDamage(int damage){

        try {
            mSocket.emit("raidDamage", new JSONObject("{\"guild\":"+user.getGuild()+", \"damage\":"+damage+"}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void disconnect(){
        mSocket.disconnect();
    }

}
