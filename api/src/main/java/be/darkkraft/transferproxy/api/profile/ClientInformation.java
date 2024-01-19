package be.darkkraft.transferproxy.api.profile;

public interface ClientInformation {

    String locale();

    byte viewDistance();

    ChatVisibility chatVisibility();

    boolean chatColors();

    byte displayedSkinParts();

    MainHand mainHand();

    boolean enableTextFiltering();

    boolean allowServerListing();

}