package com.mhook.localfrida.tool.eventbus;

public class EBusMessage {
    public int what;
    public Object arg1;
    public Object arg2;
    public Object obj;

    public EBusMessage(int what, Object obj) {
        this.what = what;
        this.obj = obj;
    }
    public EBusMessage(int what, Object arg1,Object arg2) {
        this.what = what;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }
}
