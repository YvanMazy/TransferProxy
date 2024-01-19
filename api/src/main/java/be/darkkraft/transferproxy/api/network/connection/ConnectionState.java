package be.darkkraft.transferproxy.api.network.connection;

public enum ConnectionState {

    HANDSHAKE(),
    STATUS(),
    LOGIN(true),
    TRANSFER(true),
    CONFIG(true),
    CLOSED();

    private final boolean login;

    ConnectionState() {
        this(false);
    }

    ConnectionState(final boolean login) {
        this.login = login;
    }

    public static ConnectionState fromId(final int id) {
        return switch (id) {
            case 1 -> STATUS;
            case 2 -> LOGIN;
            case 3 -> TRANSFER;
            default -> throw new IllegalArgumentException("Invalid state id: " + id);
        };
    }

    public boolean isLogin() {
        return this.login;
    }

}