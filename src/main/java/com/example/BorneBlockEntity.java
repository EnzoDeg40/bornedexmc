package com.example;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class BorneBlockEntity extends BlockEntity {
    private boolean isActivated = false;
    private double storedHeight = 0.0;
    private String activatedBy = "";

    public BorneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BORNE_BLOCK_ENTITY_TYPE, pos, state);
    }

    // Getter pour l'état d'activation
    public boolean isActivated() {
        return isActivated;
    }

    // Setter pour activer/désactiver la borne
    public void setActivated(boolean activated, String playerName) {
        this.isActivated = activated;
        if (activated) {
            this.activatedBy = playerName;
            // Stocker la hauteur au moment de l'activation
            this.storedHeight = pos.getY() - getSeaLevel() + 0.5;
        } else {
            this.activatedBy = "";
            this.storedHeight = 0.0;
        }
        markDirty();
        
        // Mettre à jour l'état visuel du bloc
        if (world != null && !world.isClient) {
            BlockState currentState = world.getBlockState(pos);
            BlockState newState = currentState.with(Borne.ACTIVATED, activated);
            world.setBlockState(pos, newState, 3);
        }
    }
    
    // Méthode surchargée pour activation par théodolite
    public void setActivated(boolean activated) {
        setActivated(activated, "Théodolite");
    }

    // Getter pour la hauteur stockée
    public double getStoredHeight() {
        return storedHeight;
    }

    // Getter pour qui a activé la borne
    public String getActivatedBy() {
        return activatedBy;
    }

    // Calculer le niveau de la mer selon la dimension
    private int getSeaLevel() {
        if (world == null) return 63;
        
        if (world.getRegistryKey() == net.minecraft.world.World.OVERWORLD) {
            return 63;
        } else if (world.getRegistryKey() == net.minecraft.world.World.NETHER) {
            return 32;
        }
        return 0;
    }

    public void saveData(NbtCompound nbt) {
        nbt.putBoolean("isActivated", isActivated);
        nbt.putDouble("storedHeight", storedHeight);
        nbt.putString("activatedBy", activatedBy);
    }

    public void loadData(NbtCompound nbt) {
        isActivated = nbt.getBoolean("isActivated").orElse(false);
        storedHeight = nbt.getDouble("storedHeight").orElse(0.0);
        activatedBy = nbt.getString("activatedBy").orElse("");
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}