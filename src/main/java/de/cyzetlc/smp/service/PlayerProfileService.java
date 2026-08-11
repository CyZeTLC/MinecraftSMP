package de.cyzetlc.smp.service;

import de.cyzetlc.smp.model.SystemPlayer;

import java.util.UUID;

public class PlayerProfileService {
    /**
     * @param uuid  the uuid of player
     * @return      the systemplayer
     */
    public static ISystemPlayer getPlayerProfileSync(UUID uuid) {
        return new SystemPlayer();
    }
}
