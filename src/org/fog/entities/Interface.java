package org.fog.entities;

import java.util.Queue;

public class Interface {
    protected int deviceId;
    protected Queue<Tuple> northTupleQueue;
    protected long linkBW;

    public Interface(long linkBW) {
        this.linkBW = linkBW;
    }
}
