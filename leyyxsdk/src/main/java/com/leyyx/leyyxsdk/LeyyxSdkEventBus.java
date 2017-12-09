package com.leyyx.leyyxsdk;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


abstract class LeyyxSdkEventBus implements ILeyyxSdkEventBus {
    static private LeyyxSdkEventBus theInstance;

    static void dispatchEvent(ELeyyxSdkEventType type, int resultCode, Bundle data) {
        if (theInstance != null) {
            theInstance._dispatchEvent(type, resultCode, data);
        }
    }

    LeyyxSdkEventBus() {
        theInstance = this;
    }

    private Map<ELeyyxSdkEventType, ArrayList<ILeyyxSdkEventListener>> allListeners = new HashMap<>();

    @Override
    public void addEventListener(int type, ILeyyxSdkEventListener listener) {
        if (listener != null) {
            ELeyyxSdkEventType[] types = ELeyyxSdkEventType.values();
            for (ELeyyxSdkEventType t : types) {
                if ((t.intVal()&type) != 0) {
                    ArrayList<ILeyyxSdkEventListener> theListeners = allListeners.get(t);
                    if (theListeners == null) {
                        theListeners = new ArrayList<>();
                        allListeners.put(t, theListeners);
                    }
                    theListeners.add(listener);
                }
            }
        }
    }

    @Override
    public void removeEventListner(int type, ILeyyxSdkEventListener listener) {
        if (type != 0 && listener != null) {
            ELeyyxSdkEventType[] types = ELeyyxSdkEventType.values();
            for (ELeyyxSdkEventType t : types) {
                if ((t.intVal()&type) != 0) {
                    ArrayList<ILeyyxSdkEventListener> theListeners = allListeners.get(t);
                    if (theListeners != null) {
                        theListeners.remove(listener);
                    }
                }
            }
        }
    }

    private void _dispatchEvent(ELeyyxSdkEventType type, int resultCode, Bundle data) {
        if (type != null) {
            ArrayList<ILeyyxSdkEventListener> theListeners = allListeners.get(type);
            if (theListeners != null) {
                for (ILeyyxSdkEventListener i : theListeners) {
                    if (i.onLeyyxSdkEvent(type, resultCode, data)) {
                        break;
                    }
                }
            }
        }
    }
    /*
    protected void _removeAllListeners() {
        Iterator iter = allListeners.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            ArrayList<ILeyyxSdkEventListener> aList = (ArrayList<ILeyyxSdkEventListener>) entry.getValue();
            aList.clear();
        }
        allListeners.clear();
    }
    */
}