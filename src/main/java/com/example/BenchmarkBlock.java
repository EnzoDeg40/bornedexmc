package com.example;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BenchmarkBlock extends Block {
    public BenchmarkBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            String coordinatesText = "Benchmark";
            String measureText = "meter";
            
            if (player instanceof ServerPlayerEntity serverPlayer) {
                String playerLanguage = serverPlayer.getClientOptions().language();

                if (playerLanguage.startsWith("fr")) {
                    coordinatesText = "Repère de nivellement";
                    measureText = "mètre";
                } else if (playerLanguage.startsWith("ch")) {
                    coordinatesText = "水准点";
                    measureText = "米";
                } else if (playerLanguage.startsWith("de")) {
                    coordinatesText = "Höhenfestpunkt";
                    measureText = "Meter";
                }
            }

            player.sendMessage(Text.literal(coordinatesText + (pos.getY() - 63) + ".5 " + measureText), true);
        }
        return ActionResult.SUCCESS;
    }
}