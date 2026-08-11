package de.cyzetlc.smp.service;

import eu.cyzetlc.cyzeapi.utils.entity.SystemPlayer;

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
