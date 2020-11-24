package com.example.hornavoiddanger;
/* 데시벨 단위로 변환 클래스 */
public class Decibel_World {
    public static float dbCount = 40;
    public static float minDB =100;
    public static float maxDB =0;
    public static float lastDbCount = dbCount;

    private static float min = 0.5f;  // 최소 사운드
    private static float value = 0;   // 데시벨 값

    public static void setDbCount(float dbValue) {
        if (dbValue > lastDbCount) {
            value = dbValue - lastDbCount > min ? dbValue - lastDbCount : min;
        }
        else {
            value = dbValue - lastDbCount < -min ? dbValue - lastDbCount : -min;
        }

        // 소리가 너무 빨리 변하는 것을 방지
        dbCount = lastDbCount + value * 0.2f ;
        lastDbCount = dbCount;
        if(dbCount<minDB) minDB=dbCount;
        if(dbCount>maxDB) maxDB=dbCount;
    }
}