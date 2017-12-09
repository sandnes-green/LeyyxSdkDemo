package com.leyyx.leyyxsdk;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

interface MessageHandlerProvider {
    void handleMessage(Message msg);
}

class MessageHandler<T extends MessageHandlerProvider> extends Handler{
    private final WeakReference<T> owner;

    MessageHandler(T owner) {
        this.owner = new WeakReference<>(owner);
    }

    void destory() {
        this.removeCallbacksAndMessages(null);
    }

    @Override
    public void handleMessage(Message msg) {
        owner.get().handleMessage(msg);
    }
}
