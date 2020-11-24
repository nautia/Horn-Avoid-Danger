package com.example.hornavoiddanger;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;


// 팝업 및 Foreground Service 클래스
public class FG_MyService extends Service {

    private static final String TAG = FG_MyService.class.getSimpleName();
    private final static String FOREGROUND_CHANNEL_ID = "foreground_channel_id";
    private NotificationManager mNotificationManager;
    private static int stateService = FG_Constants.STATE_SERVICE.NOT_CONNECTED;
    public static int chk = 0;


    public FG_MyService() {

    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        stateService = FG_Constants.STATE_SERVICE.NOT_CONNECTED;
        if(chk == 1)
        {
            stopForeground(true);
            stopSelf();
            chk = 0;
            Intent notificationIntent = new Intent(this, FG_MyService.class);
            notificationIntent.setAction(FG_Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP );
        }
    }

    @Override
    public void onDestroy() {
        stateService = FG_Constants.STATE_SERVICE.NOT_CONNECTED;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        // 사용자가 서비스를 시작할 경우
        switch (intent.getAction()) {
            case FG_Constants.ACTION.START_ACTION:
                Log.d(TAG, "사용자의 조작으로 인한 포그라운드");
                startForeground(FG_Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());

                // Lock
                final FG_ScreenActionReceiver screenactionreceiver = new FG_ScreenActionReceiver();
                registerReceiver(screenactionreceiver, screenactionreceiver.getFilter());

                connect();
                break;

            case FG_Constants.ACTION.STOP_ACTION:  // 중지 버튼을 누르면
                stopForeground(true);
                stopSelf();
                Main.sw.setChecked(false);
                Main.bListener = false;
                break;

            default:
                stopForeground(true);
                stopSelf();
        }

        return START_NOT_STICKY;
    }

    // 서비스가 정상적으로 연결되었다면 사용자에게 연결 알림
    private void connect() {
        // 연결된 지 1초 후
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        Log.d(TAG, "사용자의 의도로 인해 연결되었음");
                        // < 알림 창 Text >
                        Toast.makeText(getApplicationContext(),"위험 방지 실행 중", Toast.LENGTH_SHORT).show();
                        stateService = FG_Constants.STATE_SERVICE.CONNECTED;
                        startForeground(FG_Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());
                    }
                }, 1000);

    }

    @SuppressLint("WrongConstant")
            private Notification prepareNotification() {
                // 안드로이드 오레오(8.0) 이상의 버전 처리
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
                        mNotificationManager.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null) {
                    CharSequence name = getString(R.string.text_name_notification);
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID, name, importance);
            channel.enableVibration(false);
            mNotificationManager.createNotificationChannel(channel);
        }
        Intent notificationIntent = new Intent(this, FG_MyService.class);
        notificationIntent.setAction(FG_Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP );

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // 서비스 정지
        Intent stopIntent = new Intent(this, FG_MyService.class);
        stopIntent.setAction(FG_Constants.ACTION.STOP_ACTION);
        PendingIntent pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        remoteViews.setOnClickPendingIntent(R.id.btn_stop, pendingStopIntent);

        // 서비스가 연결되어 있다면
        switch(stateService) {
            case FG_Constants.STATE_SERVICE.NOT_CONNECTED:
                remoteViews.setTextViewText(R.id.tv_state, "DISCONNECTED");
                break;
            case FG_Constants.STATE_SERVICE.CONNECTED:
                remoteViews.setTextViewText(R.id.tv_state, "위험 방지 실행 중");
                break;
        }


        NotificationCompat.Builder notificationBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder = new NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID);
        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }
        notificationBuilder
                .setContent(remoteViews)
                .setSmallIcon(R.drawable.notification_logo)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        return notificationBuilder.build();
    }
}