package de.cyzetlc.smp.model;

import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class Clan {
    private final UUID id;
    private final String name;
    private final String tag;
    private final UUID owner;
    private final long created;
    private final Set<UUID> members = new LinkedHashSet<>();

    public Clan(UUID id, String name, String tag, UUID owner, long created) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.owner = owner;
        this.created = created;
    }
}
