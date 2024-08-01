package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import com.ticxo.modelengine.api.ModelEngineAPI;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import lombok.Setter;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import me.lojosho.hibiscuscommons.util.ServerUtils;
import me.lojosho.hibiscuscommons.util.packets.PacketManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class UserBackpackManager {

    @Getter
    private boolean backpackHidden;
    @Getter
    private int itemDisplayId;
    private ArrayList<Integer> particleCloud = new ArrayList<>();
    @Getter
    private final CosmeticUser user;
    @Getter
    private UserEntity entityManager;
    //@Getter @Setter
    //private boolean inBlock;

    public UserBackpackManager(CosmeticUser user) {
        this.user = user;
        this.backpackHidden = false;
        //this.inBlock = false;
        this.itemDisplayId = ServerUtils.getNextEntityId();
        this.entityManager = new UserEntity(user.getUniqueId());
        if (user.getEntity() != null) this.entityManager.refreshViewers(user.getEntity().getLocation()); // Fixes an issue where a player, who somehow removes their potions, but doesn't have an entity produces an NPE (it's dumb)
    }

    public int getFirstItemDisplayId() {
        return itemDisplayId;
    }

    public void spawnBackpack(CosmeticBackpackType cosmeticBackpackType) {
        MessagesUtil.sendDebugMessages("spawnBackpack Bukkit - Start");

        spawn(cosmeticBackpackType);
    }

    private void spawn(CosmeticBackpackType cosmeticBackpackType) {
        getEntityManager().setIds(List.of(itemDisplayId));
        getEntityManager().teleport(user.getEntity().getLocation());
        List<Player> outsideViewers = getEntityManager().getViewers();
        HMCCPacketManager.sendEntitySpawnPacket(user.getEntity().getLocation(), getFirstItemDisplayId(), EntityType.ITEM_DISPLAY, UUID.randomUUID(), getEntityManager().getViewers());
        /*if (Settings.isBackpackBlockDetection()) {
            if (checkBlock()) {
                setInBlock(true);
                HMCCPacketManager.sendItemDisplayMetadata(getFirstItemDisplayId(), outsideViewers);
            } else {
                HMCCPacketManager.sendItemDisplayMetadata(getFirstItemDisplayId(), outsideViewers);
            }
            refreshBlock(outsideViewers);
        } else {
            HMCCPacketManager.sendItemDisplayMetadata(getFirstItemDisplayId(), outsideViewers);
        }*/
        //HMCCPacketManager.sendItemDisplayMetadata(getFirstItemDisplayId(), new ItemStack(Material.AIR), outsideViewers);

        Entity entity = user.getEntity();

        int[] passengerIDs = new int[entity.getPassengers().size() + 1];

        for (int i = 0; i < entity.getPassengers().size(); i++) {
            passengerIDs[i] = entity.getPassengers().get(i).getEntityId();
        }

        passengerIDs[passengerIDs.length - 1] = this.getFirstItemDisplayId();

        ArrayList<Player> owner = new ArrayList<>();
        if (user.getPlayer() != null) owner.add(user.getPlayer());

        if (cosmeticBackpackType.isFirstPersonCompadible()) {
            for (int i = particleCloud.size(); i < cosmeticBackpackType.getHeight(); i++) {
                int entityId = ServerUtils.getNextEntityId();
                HMCCPacketManager.sendEntitySpawnPacket(user.getEntity().getLocation(), entityId, EntityType.AREA_EFFECT_CLOUD, UUID.randomUUID());
                HMCCPacketManager.sendCloudEffect(entityId, HMCCPacketManager.getViewers(user.getEntity().getLocation()));
                this.particleCloud.add(entityId);
            }
            // Copied code from updating the backpack
            for (int i = 0; i < particleCloud.size(); i++) {
                if (i == 0) HMCCPacketManager.sendRidingPacket(entity.getEntityId(), particleCloud.get(i), owner);
                else HMCCPacketManager.sendRidingPacket(particleCloud.get(i - 1), particleCloud.get(i) , owner);
            }
            HMCCPacketManager.sendRidingPacket(particleCloud.get(particleCloud.size() - 1), user.getUserBackpackManager().getFirstItemDisplayId(), owner);
            //if (!user.isHidden()) PacketManager.equipmentSlotUpdate(user.getUserBackpackManager().getFirstItemDisplayId(), EquipmentSlot.HEAD, user.getUserCosmeticItem(cosmeticBackpackType, cosmeticBackpackType.getFirstPersonBackpack()), owner);
            HMCCPacketManager.sendItemDisplayMetadata(getFirstItemDisplayId(), user.getUserCosmeticItem(cosmeticBackpackType, cosmeticBackpackType.getFirstPersonBackpack()), outsideViewers);
        }
        //PacketManager.equipmentSlotUpdate(getFirstArmorStandId(), EquipmentSlot.HEAD, user.getUserCosmeticItem(cosmeticBackpackType), outsideViewers);
        HMCCPacketManager.sendItemDisplayMetadata(getFirstItemDisplayId(), user.getUserCosmeticItem(cosmeticBackpackType), outsideViewers);
        HMCCPacketManager.sendRidingPacket(entity.getEntityId(), passengerIDs, outsideViewers);

        // No one should be using ME because it barely works but some still use it, so it's here
        if (cosmeticBackpackType.getModelName() != null && Hooks.isActiveHook("ModelEngine")) {
            if (ModelEngineAPI.getBlueprint(cosmeticBackpackType.getModelName()) == null) {
                MessagesUtil.sendDebugMessages("Invalid Model Engine Blueprint " + cosmeticBackpackType.getModelName(), Level.SEVERE);
                return;
            }
            /* TODO: Readd ModelEngine support
            ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(new PacketBaseEntity(getFirstArmorStandId(), UUID.randomUUID(), entity.getLocation()));
            ActiveModel model = ModelEngineAPI.createActiveModel(ModelEngineAPI.getBlueprint(cosmeticBackpackType.getModelName()));
            model.setCanHurt(false);
            modeledEntity.addModel(model, false);
             */
        }

        MessagesUtil.sendDebugMessages("spawnBackpack Bukkit - Finish");
    }

    public void despawnBackpack() {
        IntList entityIds = IntArrayList.of(itemDisplayId);
        if (particleCloud != null) {
            entityIds.addAll(particleCloud);
            this.particleCloud = null;
        }
        HMCCPacketManager.sendEntityDestroyPacket(entityIds, getEntityManager().getViewers());

    }

    public void hideBackpack() {
        if (user.isHidden()) return;
        //getArmorStand().getEquipment().clear();
        backpackHidden = true;
    }

    public void showBackpack() {
        if (!backpackHidden) return;
        CosmeticBackpackType cosmeticBackpackType = (CosmeticBackpackType) user.getCosmetic(CosmeticSlot.BACKPACK);
        ItemStack item = user.getUserCosmeticItem(cosmeticBackpackType);
        //getArmorStand().getEquipment().setHelmet(item);
        backpackHidden = false;
    }

    public void setVisibility(boolean shown) {
        backpackHidden = shown;
    }

    public ArrayList<Integer> getAreaEffectEntityId() {
        return particleCloud;
    }

    public void setItem(ItemStack item) {
        //PacketManager.equipmentSlotUpdate(getFirstItemDisplayId(), EquipmentSlot.HEAD, item, getEntityManager().getViewers());
        HMCCPacketManager.sendItemDisplayMetadata(getFirstItemDisplayId(), item, getEntityManager().getViewers());
    }

    public void clearItems() {
        ItemStack item = new ItemStack(Material.AIR);
        //PacketManager.equipmentSlotUpdate(getFirstItemDisplayId(), EquipmentSlot.HEAD, item, getEntityManager().getViewers());
        HMCCPacketManager.sendItemDisplayMetadata(getFirstItemDisplayId(), item, getEntityManager().getViewers());
    }

    /**
     * Refreshes the block detection for the backpack
     * @param outsideViewers
     * @return true if the entity was updated, false if not
     *//*
    public boolean refreshBlock(List<Player> outsideViewers) {
        if (Settings.isBackpackBlockDetection()) {
            if (isInBlock() && checkBlock()) {
                HMCCPacketManager.sendItemDisplayMetadata(getFirstItemDisplayId(), false, outsideViewers);
                setInBlock(false);
                return true;
            }
            if (!isInBlock() && !checkBlock()) {
                HMCCPacketManager.sendItemDisplayMetadata(getFirstItemDisplayId(), true, outsideViewers);
                setInBlock(true);
                return true;
            }
        }
        return false;
    }

    public boolean checkBlock() {
        if (Settings.isBackpackBlockDetection()) {
            Block block = getEntityManager().getLocation().clone().add(0, 1.5, 0).getBlock();
            return block.getType().isAir();
        }
        return false;
    }*/
}
