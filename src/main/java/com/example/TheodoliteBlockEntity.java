package com.example;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TheodoliteBlockEntity extends BlockEntity {
    
    public TheodoliteBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.THEODOLITE_BLOCK_ENTITY_TYPE, pos, state);
    }
    
    public void activateNearbyBornes() {
        World world = this.getWorld();
        if (world == null || world.isClient) {
            return;
        }
        
        BlockPos centerPos = this.getPos();
        int radius = 5;
        
        // Parcourir tous les blocs dans un cube de 11x11x11 (rayon de 5)
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos checkPos = centerPos.add(x, y, z);
                    
                    // Vérifier si c'est une borne
                    if (world.getBlockState(checkPos).getBlock() instanceof Borne) {
                        BlockEntity blockEntity = world.getBlockEntity(checkPos);
                        if (blockEntity instanceof BorneBlockEntity borneEntity) {
                            borneEntity.setActivated(true);
                        }
                    }
                }
            }
        }
    }
    
    public void saveData(NbtCompound nbt) {
        // Le theodolite n'a pas besoin de stocker des données spéciales
    }
    
    public void loadData(NbtCompound nbt) {
        // Le theodolite n'a pas besoin de lire des données spéciales
    }
    
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}