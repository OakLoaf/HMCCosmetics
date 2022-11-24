package com.hibiscusmc.hmccosmetics.entities;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.persistence.PersistentDataType;

public class MEGEntity extends Bat {

    public MEGEntity(Location loc) {
        super(EntityType.BAT, ((CraftWorld) loc.getWorld()).getHandle());
        this.setPos(loc.getX(), loc.getY(), loc.getZ());
        HMCCosmeticsPlugin.getInstance().getLogger().info("Spawned MEGEntity at " + loc);
        getBukkitLivingEntity().setInvisible(true);
        getBukkitLivingEntity().setInvulnerable(true); // NOTE - CREATIVE PLAYERS CAN DESTROY IT STILL
        getBukkitLivingEntity().setAI(false);
        getBukkitLivingEntity().setGravity(false);
        getBukkitLivingEntity().setSilent(true);
        getBukkitLivingEntity().setCollidable(false);
        persist = false;

        getBukkitEntity().getPersistentDataContainer().set(new NamespacedKey(HMCCosmeticsPlugin.getInstance(), "cosmeticMob"), PersistentDataType.SHORT, Short.valueOf("1"));
    }
}
