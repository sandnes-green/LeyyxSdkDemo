package com.leyyx.leyyxsdk;

import android.os.Bundle;

public interface ILeyyxSdkEventListener {
    Boolean onLeyyxSdkEvent(ELeyyxSdkEventType type, int resultCode, Bundle data);
}
