package com.example;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItems {
    // Create a custom item group key
    public static final RegistryKey<ItemGroup> CUSTOM_ITEM_GROUP_KEY = RegistryKey.of(
        RegistryKeys.ITEM_GROUP, 
        Identifier.of(ExampleMod.MOD_ID, "custom_group")
    );

    // Create the custom item group
    public static final ItemGroup CUSTOM_ITEM_GROUP = FabricItemGroup.builder()
        .icon(() -> new ItemStack(ModBlocks.BORNE))
        .displayName(Text.translatable("itemgroup.modid.custom_group"))
        .build();

    public static void initialize() {
        // Register the custom item group
        Registry.register(Registries.ITEM_GROUP, CUSTOM_ITEM_GROUP_KEY, CUSTOM_ITEM_GROUP);
    }
}