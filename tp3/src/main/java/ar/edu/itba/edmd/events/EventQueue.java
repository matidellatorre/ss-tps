package ar.edu.itba.edmd.events;

import java.util.PriorityQueue;

public class EventQueue {
    private final PriorityQueue<Event> queue;

    public EventQueue() {
        this.queue = new PriorityQueue<>();
    }

    public Event peek() {
        return queue.peek();
    }

    public void add(Event event) {
        queue.add(event);
    }

    public Event poll() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }

    public void clear() {
        queue.clear();
    }
}
