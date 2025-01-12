package com.hibiscusmc.hmccosmetics.hooks.entities;

import com.hibiscusmc.hmccosmetics.hooks.EntityHook;
import me.lojosho.hibiscuscommons.hooks.Hook;
import me.lojosho.hibiscuscommons.hooks.HookFlag;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class CitizensHook extends Hook implements EntityHook {

    public CitizensHook() {
        super("Citizens", HookFlag.ENTITY_SUPPORT);
    }

    @Override
    public Entity getEntity(UUID uuid) {
        NPC npc = CitizensAPI.getNPCRegistry().getByUniqueId(uuid);
        return npc != null ? npc.getEntity() : null;
    }
}
