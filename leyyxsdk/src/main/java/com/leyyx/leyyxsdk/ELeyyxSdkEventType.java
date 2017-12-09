package com.leyyx.leyyxsdk;


public enum ELeyyxSdkEventType {
    LOGIN(1), LOGOUT(2), PAY(4),REGISTER(3);

    private int v;
    ELeyyxSdkEventType(int v) {this.v = v;}
    public int intVal(){return this.v;}
}
