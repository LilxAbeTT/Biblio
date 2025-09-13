package com.biblio.service;

import javafx.animation.AnimationTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

public class TimerService extends AnimationTimer {
    private final List<LongConsumer> listeners = new ArrayList<>();
    @Override
    public void handle(long now) {
        for (LongConsumer c : listeners) c.accept(now);
    }
    public void addListener(LongConsumer c){ listeners.add(c); }
}
