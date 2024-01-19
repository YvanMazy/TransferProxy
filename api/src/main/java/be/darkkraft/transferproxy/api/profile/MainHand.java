package be.darkkraft.transferproxy.api.profile;

public enum MainHand {

    LEFT,
    RIGHT;

    public static MainHand fromId(final int id) {
        return id == 0 ? LEFT : RIGHT;
    }

}