package com.leyyx.leyyxsdk;

public interface ILeyyxSdkEventBus {
    void addEventListener(int type, ILeyyxSdkEventListener listener);

    void removeEventListner(int type, ILeyyxSdkEventListener listener);

}
