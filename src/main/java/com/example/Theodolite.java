package com.example;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class Theodolite extends Block implements BlockEntityProvider {
    
    // Propriété pour définir si c'est la partie haute (true) ou basse (false)
    public static final BooleanProperty UPPER = BooleanProperty.of("upper");
    
    // Forme simplifiée pour la partie basse (un seul cube central)
    private static final VoxelShape LOWER_SHAPE = Block.createCuboidShape(6, 0, 6, 10, 16, 10);
    
    // Forme pour la partie haute (instrument)
    private static final VoxelShape UPPER_SHAPE = Block.createCuboidShape(6, 0, 2, 10, 4, 14);
    
    public Theodolite(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(UPPER, false));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(UPPER);
    }
    
    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(UPPER) ? UPPER_SHAPE : LOWER_SHAPE;
    }
    
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(UPPER) ? UPPER_SHAPE : LOWER_SHAPE;
    }
    
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        World world = ctx.getWorld();
        
        // Vérifier s'il y a assez d'espace pour 2 blocs
        if (pos.getY() < world.getHeight() - 1 && world.getBlockState(pos.up()).canReplace(ctx)) {
            return this.getDefaultState().with(UPPER, false);
        }
        return null;
    }
    
    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient && !state.get(UPPER)) {
            // Placer la partie haute
            BlockPos upperPos = pos.up();
            world.setBlockState(upperPos, state.with(UPPER, true), 3);
        }
        super.onBlockAdded(state, world, pos, oldState, notify);
    }
    
    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        if (!world.isClient()) {
            // Si on casse la partie basse, casser aussi la partie haute
            if (!state.get(UPPER)) {
                BlockPos upperPos = pos.up();
                BlockState upperState = world.getBlockState(upperPos);
                if (upperState.isOf(this) && upperState.get(UPPER)) {
                    world.setBlockState(upperPos, net.minecraft.block.Blocks.AIR.getDefaultState(), 3);
                }
            }
            // Si on casse la partie haute, casser aussi la partie basse
            else {
                BlockPos lowerPos = pos.down();
                BlockState lowerState = world.getBlockState(lowerPos);
                if (lowerState.isOf(this) && !lowerState.get(UPPER)) {
                    world.setBlockState(lowerPos, net.minecraft.block.Blocks.AIR.getDefaultState(), 3);
                }
            }
        }
        super.onBroken(world, pos, state);
    }
    
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        // Si on clique sur la partie haute, rediriger vers la partie basse
        if (state.get(UPPER)) {
            BlockPos lowerPos = pos.down();
            BlockState lowerState = world.getBlockState(lowerPos);
            if (lowerState.isOf(this) && !lowerState.get(UPPER)) {
                return this.onUse(lowerState, world, lowerPos, player, hit);
            }
            return ActionResult.PASS;
        }
        
        if (!world.isClient) {
            // Vérifier si le theodolite est à ciel ouvert
            if (world.getRegistryKey() == World.OVERWORLD) {
                if (!isOpenToSky(world, pos)) {
                    player.sendMessage(Text.literal("Le théodolite doit être placé à ciel ouvert !"), false);
                    return ActionResult.SUCCESS;
                }
            }
            else if (world.getRegistryKey() == World.NETHER) {
                if (!isOpenUp(world, pos)) {
                    player.sendMessage(Text.literal("Le théodolite doit avoir 20 blocs d'air au-dessus dans le Nether !"), false);
                    return ActionResult.SUCCESS;
                }
            }
            else {
                player.sendMessage(Text.literal("Le théodolite ne fonctionne pas dans cette dimension."), false);
                return ActionResult.SUCCESS;
            }
            
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TheodoliteBlockEntity theodoliteEntity) {
                theodoliteEntity.activateNearbyBornes();
                player.sendMessage(Text.literal("Théodolite activé ! Bornes dans un rayon de 5 blocs activées."), false);
            }
        }
        return ActionResult.SUCCESS;
    }
    
    private boolean isOpenToSky(World world, BlockPos pos) {
        // Vérifier qu'il n'y a aucun bloc solide au-dessus jusqu'au ciel
        // Commencer depuis le sommet du théodolite (pos + 2 car il fait 2 blocs de hauteur)
        for (int y = pos.getY() + 2; y < world.getHeight(); y++) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (!world.getBlockState(checkPos).isAir()) {
                return false;
            }
        }
        return true;
    }

    private boolean isOpenUp(World world, BlockPos pos)
    {
        // Vérifier qu'il y a que de l'air au-dessus sur 20 blocs
        // Commencer depuis le sommet du théodolite (pos + 2 car il fait 2 blocs de hauteur)
        for (int y = pos.getY() + 2; y < pos.getY() + 22; y++) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (!world.getBlockState(checkPos).isAir()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        // Seulement la partie basse a une BlockEntity
        return state.get(UPPER) ? null : new TheodoliteBlockEntity(pos, state);
    }
}