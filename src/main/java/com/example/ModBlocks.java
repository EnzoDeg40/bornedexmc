package com.example;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import java.util.function.Function;

public class ModBlocks {
	private static Block register(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings settings, boolean shouldRegisterItem) {
		// Create a registry key for the block
		RegistryKey<Block> blockKey = keyOfBlock(name);
		// Create the block instance
		Block block = blockFactory.apply(settings.registryKey(blockKey));

		// Sometimes, you may not want to register an item for the block.
		// Eg: if it's a technical block like `minecraft:moving_piston` or `minecraft:end_gateway`
		if (shouldRegisterItem) {
			// Items need to be registered with a different type of registry key, but the ID
			// can be the same.
			RegistryKey<Item> itemKey = keyOfItem(name);

			BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey());
			Registry.register(Registries.ITEM, itemKey, blockItem);
		}

		return Registry.register(Registries.BLOCK, blockKey, block);
	}

	private static RegistryKey<Block> keyOfBlock(String name) {
		return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(ExampleMod.MOD_ID, name));
	}

	private static RegistryKey<Item> keyOfItem(String name) {
		return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ExampleMod.MOD_ID, name));
	}

    public static void initialize() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register((itemGroup) -> {
			itemGroup.add(ModBlocks.WALL_ATTACHABLE_BENCHMARK_BLOCK.asItem());
		});
    }
    
    public static final Block WALL_ATTACHABLE_BENCHMARK_BLOCK = register(
        "wall_attachable_benchmark_block",
        WallAttachableBenchmarkBlock::new,
        AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE).strength(2.0f),
        true
    );
}

// public class ModBlocks {
//     // Déclarer les variables sans les initialiser
//     public static Block DECORATIVE_BLOCK;
//     public static BlockItem DECORATIVE_BLOCK_ITEM;

//     public static void registerModBlocks() {
//         ExampleMod.LOGGER.info("Registering Mod Blocks for " + ExampleMod.MOD_ID);

//         // Créer et enregistrer le bloc
//         DECORATIVE_BLOCK = Registry.register(
//                 Registries.BLOCK,
//                 Identifier.of(ExampleMod.MOD_ID, "decorative_block"),
//                 new Block(AbstractBlock.Settings.create()
//                         .strength(3.0f, 3.0f)
//                         .sounds(BlockSoundGroup.STONE)
//                         .requiresTool())
//         );

//         // Créer et enregistrer l'item du bloc
//         DECORATIVE_BLOCK_ITEM = Registry.register(
//                 Registries.ITEM,
//                 Identifier.of(ExampleMod.MOD_ID, "decorative_block"),
//                 new BlockItem(DECORATIVE_BLOCK, new Item.Settings())
//         );

//         // Ajouter le bloc au groupe d'items "Building Blocks"
//         ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
//             entries.add(DECORATIVE_BLOCK);
//         });
//     }
// }