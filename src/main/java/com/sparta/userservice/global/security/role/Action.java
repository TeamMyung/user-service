package com.sparta.userservice.global.security.role;

public enum Action {
    CREATE, READ, UPDATE, DELETE;

    static Action of(String action) {
        return Action.valueOf(action.trim().toUpperCase());
    }
}
