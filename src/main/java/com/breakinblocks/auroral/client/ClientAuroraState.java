package com.breakinblocks.auroral.client;

/**
 * Client-side storage for aurora state.
 * Updated via network packets from the server.
 */
public class ClientAuroraState {
    private static boolean auroraActive = false;

    /**
     * Checks if aurora is currently active on the client.
     *
     * @return true if aurora is active
     */
    public static boolean isAuroraActive() {
        return auroraActive;
    }

    /**
     * Sets the aurora active state. Called from network handler.
     *
     * @param active Whether aurora is active
     */
    public static void setAuroraActive(boolean active) {
        auroraActive = active;
    }

    /**
     * Resets the state. Called on disconnect.
     */
    public static void reset() {
        auroraActive = false;
    }
}
