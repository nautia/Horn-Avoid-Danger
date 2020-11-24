package com.example.hornavoiddanger;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
/* 파일입출력 관련 클래스 */

public class Decibel_FileUtil {
    private static final String TAG = "menu3_Decibel_FileUtil";
    public static final String LOCAL = "SoundMeter";    // ★폴더 이름★
    public static final String LOCAL_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator;
    public static final String REC_PATH = LOCAL_PATH + LOCAL + File.separator;    // 녹음 파일을 저장할 위치 지정

    //  내장메모리에 녹음 파일을 저장할 디렉터리 자동 생성
    static {
        File dirRootFile = new File(LOCAL_PATH);
        if (!dirRootFile.exists()) {
            dirRootFile.mkdirs();
        }
        File recFile = new File(REC_PATH);
        if (!recFile.exists()) {
            recFile.mkdirs();
        }
    }

    private Decibel_FileUtil() { }

    private static boolean hasFile(String fileName) {
        File f = createFile(fileName);
        return null != f && f.exists();
    }

    public static File createFile(String fileName) {
        File myCaptureFile = new File(REC_PATH + fileName);
        if (myCaptureFile.exists()) {
            myCaptureFile.delete();
        }
        try {
            myCaptureFile.createNewFile();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return myCaptureFile;
    }
}