package com.hibiscusmc.hmccosmetics.hooks;

import org.bukkit.entity.Entity;

import java.util.UUID;

public interface EntityHook {

    Entity getEntity(UUID uuid);
}
