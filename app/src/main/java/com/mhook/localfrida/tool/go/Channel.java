package com.mhook.localfrida.tool.go;

/**
 * Created by ASUS on 2020/7/25.
 */

public class Channel<T> {
    private T message;

    private boolean empty = true;

    public synchronized T take() {
        while (empty) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        empty = true;
        notifyAll();
        return message;
    }

    public synchronized void put(T message) {
        while (!empty) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        empty = false;
        this.message = message;
        notifyAll();
    }
}

