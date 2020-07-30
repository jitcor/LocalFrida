package com.mhook.localfrida.tool.eventbus;

import org.greenrobot.eventbus.EventBus;

public class BaseEventBus {
    private EventBus eventBus;
    private static BaseEventBus instance;
    public static BaseEventBus getInstance(){
        if(instance==null)instance=new BaseEventBus();
        return instance;
    }
    private BaseEventBus(){
        eventBus=EventBus.getDefault();
    }
    public void register(Object object){
        eventBus.register(object);
    }
    public void unregister(Object object){
        eventBus.unregister(object);
    }
    public void post(Object object){
        eventBus.post(object);
    }
}
