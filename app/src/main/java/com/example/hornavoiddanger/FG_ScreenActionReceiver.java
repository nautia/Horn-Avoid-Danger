package com.example.hornavoiddanger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

// 휴대폰 lock or unlock 상태에서도 foreground가 정상적으로 구동되게 하는 클래스
public class FG_ScreenActionReceiver extends BroadcastReceiver {

    private String TAG = "menu3_FG_ScreenActionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {


        //LOG
        StringBuilder sb = new StringBuilder();
        sb.append("Action: " + intent.getAction() + "\n");
        sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
        String log = sb.toString();
        Log.d(TAG, log);
        Toast.makeText(context, log, Toast.LENGTH_LONG).show();

        String action = intent.getAction();

        // 단말기의 스크린이 켜있다면
        if(Intent.ACTION_SCREEN_ON.equals(action)) {
            Log.d(TAG, "화면 on");
            context.startService(new Intent(context, FG_FloatingWindow.class));
        }

        // 단말기의 스크린이 꺼져있다면 (혹시 몰라서 만들어놓음)
        else if(Intent.ACTION_SCREEN_OFF.equals(action)) {
            Log.d(TAG, "화면 off");
        }

        // 사용자가 잠겨있는 화면을 다시 키면
        else if(Intent.ACTION_USER_PRESENT.equals(action)) {
            Log.d(TAG, "잠금 해제");

            // 단말기의 버전확인 (오레오o : 8.0) 후 포그라운드 서비스 활성화
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, FG_FloatingWindow.class));
            } else {
                context.startService(new Intent(context, FG_FloatingWindow.class));
            }
        }


/*    부팅이 되었다면(미구현, 안해도됨)
      else if(Intent.ACTION_BOOT_COMPLETED.equals(action)){
            Log.d(TAG, "부팅 완료");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, FloatingWindow.class));
            } else {
                context.startService(new Intent(context, FloatingWindow.class));
            }
      }
*/

    }

    public IntentFilter getFilter(){
        final IntentFilter filter = new IntentFilter();
        return filter;
    }
}
