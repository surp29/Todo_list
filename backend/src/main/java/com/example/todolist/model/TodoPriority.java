package com.example.todolist.model;

/**
 * Represents the priority level of a {@link Todo} item. Each level carries its
 * own scoring weight, used by the productivity analytics to weigh a completed
 * high-priority task more than a low-priority one.
 */
public enum TodoPriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int weight;

    TodoPriority(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
