package com.example;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class WallAttachableBenchmarkBlock extends Block {
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    
    // Formes pour chaque direction (comme un bouton de 2 pixels d'épaisseur)
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(5.0, 4.0, 14.0, 11.0, 12.0, 16.0);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(5.0, 4.0, 0.0, 11.0, 12.0, 2.0);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(14.0, 4.0, 5.0, 16.0, 12.0, 11.0);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0.0, 4.0, 5.0, 2.0, 12.0, 11.0);

    public WallAttachableBenchmarkBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> VoxelShapes.fullCube();
        };
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getHorizontalPlayerFacing().getOpposite();
        BlockState blockState = this.getDefaultState().with(FACING, direction);
        
        // Vérifier si le bloc peut être placé à cette position
        WorldView worldView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        
        // Vérifier s'il y a un bloc solide derrière pour se fixer
        if (canPlaceAt(blockState, worldView, blockPos)) {
            return blockState;
        }
        
        // Essayer les autres directions
        for (Direction dir : Direction.Type.HORIZONTAL) {
            blockState = this.getDefaultState().with(FACING, dir);
            if (canPlaceAt(blockState, worldView, blockPos)) {
                return blockState;
            }
        }
        
        return null;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos attachedPos = pos.offset(direction.getOpposite());
        return world.getBlockState(attachedPos).isSideSolidFullSquare(world, attachedPos, direction);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            String coordinatesText = "Wall Benchmark";
            String measureText = "meter";
            
            if (player instanceof ServerPlayerEntity serverPlayer) {
                String playerLanguage = serverPlayer.getClientOptions().language();

                if (playerLanguage.startsWith("fr")) {
                    coordinatesText = "Repère mural de nivellement";
                    measureText = "mètre";
                } else if (playerLanguage.startsWith("ch")) {
                    coordinatesText = "墙壁水准点";
                    measureText = "米";
                } else if (playerLanguage.startsWith("de")) {
                    coordinatesText = "Wand-Höhenfestpunkt";
                    measureText = "Meter";
                }
            }

            player.sendMessage(Text.literal(coordinatesText + " " + (pos.getY() - 63) + ".5 " + measureText), true);
        }
        return ActionResult.SUCCESS;
    }
}