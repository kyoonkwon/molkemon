package com.example.week2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.kakao.sdk.common.util.Utility;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.Account;
import com.kakao.sdk.user.model.Profile;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MainActivity extends FragmentActivity {
    String userId;
    String nickname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(new GameView(this));
        Intent intent = getIntent();
        //Log.i("after sending",nickname);
        nickname = intent.getStringExtra("nickname");
        setContentView(R.layout.activity_main);
        Button logout = findViewById(R.id.logout);
        Button train = findViewById(R.id.train);
        Button adventure = findViewById(R.id.adventure);
        Button raid = findViewById(R.id.raid);
        TrainActivity fragment1 = new TrainActivity();
        AdventureActivity fragment2 = new AdventureActivity();
        RaidActivity fragment3 = new RaidActivity();
        TextView nick = findViewById(R.id.nickname);
        nick.setText(nickname);
        logout.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "정상적으로 로그아웃되었습니다.", Toast.LENGTH_SHORT).show();

                UserApiClient.getInstance().logout(error -> {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                    return null;
                });
            }
        });
        raid.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(getApplicationContext(), "레이드 버튼 클릭됨.", Toast.LENGTH_SHORT).show();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_main,fragment3);
                //transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        adventure.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(getApplicationContext(), "어드벤처 버튼 클릭됨.", Toast.LENGTH_SHORT).show();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_main,fragment2);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });


    }
    @Override
    protected void onStop() {
        setResult(RESULT_OK);
        super.onStop();
    }
    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
    @Override
    protected void onDestroy() {
        setResult(RESULT_OK);
        super.onDestroy();
    }
}