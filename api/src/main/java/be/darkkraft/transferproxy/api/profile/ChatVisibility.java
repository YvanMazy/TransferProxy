package be.darkkraft.transferproxy.api.profile;

public enum ChatVisibility {

    FULL,
    SYSTEM,
    HIDDEN;

    private final byte id;

    ChatVisibility() {
        this.id = (byte) this.ordinal();
    }

    public byte id() {
        return this.id;
    }

    public static ChatVisibility fromId(final int id) {
        return switch (id) {
            case 1 -> SYSTEM;
            case 2 -> HIDDEN;
            default -> FULL;
        };
    }

}