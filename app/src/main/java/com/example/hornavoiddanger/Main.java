package com.example.hornavoiddanger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.AbstractPreferences;

public class Main extends AppCompatActivity {
    public static int chk;
    public static boolean refreshed;
    public static boolean isThreadRun;
    /* 사용 변수 모음 */
    TextView curVal, textView;
    public static Switch sw;

    /* Decibel 변수 */
    public static boolean bListener;
    private Decibel_MyMediaRecorder mRecorder;

    /* 스레드 관련 변수 */
    private Thread thread;
    float volume = 10000;

    /* 상태 저장 관련 변수 */
    private SharedPreferences sharedPreferences1;     // 현 자바 파일의 상태 저장 관련 변수
    private SharedPreferences sharedPreferences2;     // 현 자바 파일의 상태 저장 관련 변수
    public static final String ex = "switch";       // 스위치 상태 저장 변수
    public static final String tx = "boolean";      // 녹음 준비 상태 저장 변수
    public static final String px = "radio";        // 라디오 박스의 상태 저장 변수
    boolean set_time1 = false;
    boolean set_time2 = false;
    boolean set_time3 = false;

    /* seekBar 및 Radio button 선언 */
    int PW = 50;                       // 프로그램 안정성을 위한 세기 임시 변수
    static int chk_c;             //  해당 기능이 작동되는지 확인 변수

    static int pws = 50;              // 임시 진동 세기 초기값
    TextToSpeech myTTS;

    private String[] permissions = {                          /* permissions 모음 */
            Manifest.permission.RECORD_AUDIO,                 // 녹음 권한
            Manifest.permission.WRITE_EXTERNAL_STORAGE,     // 기기, 사진, 미디어, 파일 엑세스 권한
            Manifest.permission.VIBRATE,                       // 진동 권한
            Manifest.permission.CAMERA
    };
    private static final int MULTIPLE_PERMISSIONS = 101;

    /* ★onCreate★ */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        curVal = findViewById(R.id.curval);
        textView = findViewById(R.id.text_review);
        sw = findViewById(R.id.switch_onoff);

        textView.setMovementMethod(new ScrollingMovementMethod());

        /* 출력 및 진동 관련 */
        SeekBar seekBar2 =  (SeekBar) findViewById(R.id.seekBar2);

        // //하단 진동 시간 버튼
        RadioGroup group=(RadioGroup)findViewById(R.id.RadioGroup1);
        RadioButton rdb_n1 = (RadioButton) findViewById(R.id.rdb_n1);
        RadioButton rdb_n2 = (RadioButton) findViewById(R.id.rdb_n2);
        RadioButton rdb_n3 = (RadioButton) findViewById(R.id.rdb_n3);

        // 포그라운드 선언
        final Intent startIntent = new Intent(Main.this, FG_MyService.class);
        startIntent.setAction(FG_Constants.ACTION.START_ACTION);

        final Intent stopIntent = new Intent(Main.this, FG_MyService.class);
        stopIntent.setAction(FG_Constants.ACTION.STOP_ACTION);

        mRecorder = new Decibel_MyMediaRecorder();
        new FG_MyService();
        refreshed=false;



        /* 상태 저장 관련 */
        sharedPreferences1 = getSharedPreferences(" ",MODE_PRIVATE);
        sharedPreferences2 = getSharedPreferences(" ", MODE_PRIVATE);

        final SharedPreferences.Editor editor1 = sharedPreferences1.edit();
        final SharedPreferences.Editor editor2 = sharedPreferences2.edit();

        if (Build.VERSION.SDK_INT >= 23) {              // 안드로이드 6.0 이상일 경우 퍼미션 체크 (API 23 이상!!)
            checkPermissions();  // checkPermissions() 함수 호출
        }
        if(chk_c == 0) {
            editor1.putBoolean(ex,false);  // 스위치 상태를 true 상태 저장
            editor1.putBoolean(tx,false);  // 녹음 준비 상태를 true 상태 저장
            sw.setChecked(false);
            editor1.commit();
        } else if(chk_c == 1) {
            editor1.putBoolean(ex,true);  // 스위치 상태를 true 상태 저장
            editor1.putBoolean(tx,true);  // 녹음 준비 상태를 true 상태 저장
            sw.setChecked(true);
            editor1.commit();
        }

        bListener = sharedPreferences1.getBoolean(tx,false);     // 녹음 준비 상태를 가져옴
        sw.setChecked(sharedPreferences1.getBoolean(ex,false));
        seekBar2.setProgress(sharedPreferences2.getInt("seekBar",0));     // seekbar 상태를 가져옴
        set_time1 = sharedPreferences2.getBoolean("set_time1", false);  // 라디오 박스 상태를 가져옴
        set_time2 = sharedPreferences2.getBoolean("set_time2", false);
        set_time3 = sharedPreferences2.getBoolean("set_time3", false);

        /* 라디오 박스 상태 가공 */
        if (set_time1 == true) {
            chk = 1;
            rdb_n2.setChecked(false);
            rdb_n3.setChecked(false);
            rdb_n1.setChecked(true);
        } else if(set_time2 == true) {
            chk = 2;
            rdb_n1.setChecked(false);
            rdb_n3.setChecked(false);
            rdb_n2.setChecked(true);
        } else if(set_time3 == true) {
            chk = 3;
            rdb_n1.setChecked(false);
            rdb_n2.setChecked(false);
            rdb_n3.setChecked(true);
        } else if(set_time1 == false && set_time2 == false && set_time3 == false) {
            chk = 1;
        }

        //라디오 버튼
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rdb_n1:
                        chk = 1;
                        editor2.putBoolean("set_time1", true);
                        editor2.putBoolean("set_time2", false);
                        editor2.putBoolean("set_time3", false);
                        break;
                    case R.id.rdb_n2:
                        chk = 2;
                        editor2.putBoolean("set_time1", false);
                        editor2.putBoolean("set_time2", true);
                        editor2.putBoolean("set_time3", false);
                        break;
                    case R.id.rdb_n3:
                        chk = 3;
                        editor2.putBoolean("set_time1", false);
                        editor2.putBoolean("set_time2", false);
                        editor2.putBoolean("set_time3", true);
                        break;
                }
                editor2.commit();
            }
        });

        //시크바(start)
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar2) {  // 움직임이 시작
                PW = 0;
                PW = seekBar2.getProgress();
                pws = PW;
                editor2.putInt("seekBar", seekBar2.getProgress());
                editor2.commit();
            }
            public void onStartTrackingTouch(SeekBar seekBar2) { // 움직임이 멈춤
                PW = 0;
                PW = seekBar2.getProgress();
                pws = PW;
                editor2.putInt("seekBar", seekBar2.getProgress());
                editor2.commit();
            }
            public void onProgressChanged(SeekBar seekBar2, int progress, boolean fromUser) {  //  seekbar 움직임 변동 (updata)
                PW = 0;
                PW = seekBar2.getProgress();
                pws = PW;
                editor2.putInt("seekBar", seekBar2.getProgress());
                editor2.commit();
            }
        });

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor1.putBoolean(ex,true);  // 스위치 상태를 true 상태 저장
                    editor1.putBoolean(tx,true);  // 녹음 준비 상태를 true 상태 저장
                    //  녹음 파일이 생성
                    File file = Decibel_FileUtil.createFile("temp.amr");

                    // 녹음용 파일이 생성되었다면
                    if (file != null) {
                        // 녹음시작
                        startRecord(file);
                    }
                    // 녹음용 파일이 생성되지 않았다면
                    else {
                        // 파일을 만들 수 없음 메세지 출력
                        Toast.makeText(getApplicationContext(), getString(R.string.activity_recFileErr), Toast.LENGTH_LONG).show();
                    }
                    bListener = true;    // 녹음할 준비가 됨
                    isThreadRun = true; // 스레드 시작

                    // 포그라운드(백그라운드) 서비스를 실행하기 전 사용자의 단말기 버전을 확인 (o : 오레오 8.0)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(startIntent);
                    } else {
                        startService(startIntent);
                    }
                    FG_MyService.chk = 0;
                    chk_c = 1;
                } else {
                    editor1.putBoolean(ex,false);  // 스위치 상태를 false 상태 저장
                    editor1.putBoolean(tx,false);  // 녹음 준비 상태를 false 상태 저장
                    Intent stopIntent = new Intent(Main.this, FG_MyService.class);
                    stopIntent.setAction(FG_Constants.ACTION.STOP_ACTION);
                    stopService(stopIntent);
                    FG_MyService.chk = 1;
                    new FG_MyService();
                    curVal.setText("스위치 OFF");
                    bListener = false;     // 녹음할 준비가 안된 상태로 만든다
                    isThreadRun = false;  // 스레드 중지
                    chk_c = 0;
                }
                editor1.commit();
            }
        });

        if(bListener == true) {
            File file = Decibel_FileUtil.createFile("temp.amr");
            if (file != null) {
                // 녹음시작
                startRecord(file);
            }
        }
    }

    /* 출력 및 진동 관련 */
    @SuppressLint("HandlerLeak")
    final Handler handler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.O) //진동관련
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DecimalFormat df1 = new DecimalFormat("####.0");
            Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

            //0~39 > 0~50.7
            if (Decibel_World.dbCount >= 0 && Decibel_World.dbCount <= 39) {
                curVal.setText(df1.format(Decibel_World.dbCount*1.3));
            }
            //40~90 > 60 ~ 135
            if(Decibel_World.dbCount >= 40 && bListener == true) {
                //사용자 화면상 출력 데시벨이 109.5가 초과
                if(Decibel_World.dbCount > 73) {
                    //안드 8.0 이상, API 26이상 단말기의 진동 발생
                    int amplitudes = pws;
                    if(Build.VERSION.SDK_INT>=26) {
                        curVal.setText(df1.format(Decibel_World.dbCount * 1.5));
                        if(chk == 1) {
                            vibrator.vibrate(VibrationEffect.createOneShot(1000, amplitudes));
                        } else if(chk == 2) {
                            vibrator.vibrate(VibrationEffect.createOneShot(3000, amplitudes));
                        } else if(chk == 3) {
                            vibrator.vibrate(VibrationEffect.createOneShot(5000, amplitudes));
                        }
                    }
                    //안드 8.0 미만, API 26미만 단말기의 진동 발생
                    else {
                        curVal.setText(df1.format(Decibel_World.dbCount * 1.5));
                        if(chk == 1) {
                            vibrator.vibrate(1000);
                        } else if(chk == 2) {
                            vibrator.vibrate(2000);
                        } else if(chk == 3) {
                            vibrator.vibrate(3000);
                        }
                    }
                } else {
                    curVal.setText(df1.format(Decibel_World.dbCount * 1.5));
                }
            }
        }
    };

    /* 녹음 시작 */
    public void startRecord(File fFile) {
        try {
            mRecorder.setMyRecAudioFile(fFile);

            if (mRecorder.startRecorder()) {
                startListenAudio();
            } else {
                Toast.makeText(this, getString(R.string.activity_recStartErr), Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e) {
            Toast.makeText(this, getString(R.string.activity_recBusyErr), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /* 쓰레드 지정값에 대한 실시간 데시벨 변환 */
    private void startListenAudio() {
        thread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                while (isThreadRun) {
                    try {
                        if(bListener) {
                            volume = mRecorder.getMaxAmplitude();  // 음압값을 가져옴 (getMaxAmplitude())
                            if(volume > 0 && volume < 1000000) {
                                Decibel_World.setDbCount(20 * (float)(Math.log10(volume)));  // 음압값을 데시벨로 변경
                                Message message = new Message();
                                message.what = 1;
                                handler.sendMessage(message);
                            }
                        }

                        if(refreshed) {
                            Thread.sleep(1200);
                            refreshed=false;
                        } else {
                            Thread.sleep(400);

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        bListener = false;
                        isThreadRun = false;
                    }
                }
            }
        });
        thread.start();
    }

    private boolean checkPermissions() {      // 단말기에 필요한 권한이 승인되었는지 확인
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[i])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showToast_PermissionDeny();
                            }
                        }
                    }
                }
                else {
                    showToast_PermissionDeny();
                }
                return;
            }
        }

    }

    private void showToast_PermissionDeny() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onBackPressed() {
        super.onBackPressed();
    }
}