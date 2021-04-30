package me.activated.core.database.redis.other.settings;

import lombok.Getter;

@Getter
public class JedisSettings {

    private String address;
    private int port;
    private String password;

    public JedisSettings(String address, int port, String password) {
        this.address = address;
        this.port = port;
        this.password = password;
    }

    public boolean hasPassword() {
        return this.password != null && !this.password.equals("");
    }
}
