package net.portalmod.core.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.portalmod.core.registry.RegistryObject;
import net.portalmod.common.blocks.ForestCakeBlock;
import net.portalmod.common.blocks.MultiBlock;
import net.portalmod.common.sorted.gel.AbstractGelBlock;
import net.portalmod.core.init.BlockInit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.ConstantIntValue;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.resources.ResourceLocation;

public class LootTableGen extends LootTableProvider {
    public LootTableGen(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return ImmutableList.of(Pair.of(PMBlockLootTables::new, LootContextParamSets.BLOCK));
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> p_validate_1_, ValidationContext p_validate_2_) {
    }

    public static class PMBlockLootTables extends BlockLoot {

        @Override
        protected void addTables() {
            for (RegistryObject<Block> block : BlockInit.BLOCKS.getEntries()) {
                setLootTable(block.get());
            }
        }

        public static boolean skipLootTable(Block block) {
            return block.asItem() == Items.AIR || block instanceof AbstractGelBlock;
        }

        public void setLootTable(Block block) {
            if (skipLootTable(block)) {
                return;
            }

            if (block instanceof ForestCakeBlock) {
                this.dropWhenSilkTouch(block);
            }
            else if (block instanceof DoorBlock) {
                this.add(block, createDoorTable(block));
            }
            else if (block instanceof SlabBlock) {
                this.add(block, createSlabItemTable(block));
            }
            else if (block instanceof MultiBlock) {
                this.add(block, multiBlockCondition((MultiBlock) block));
            }
            else {
                this.dropSelf(block);
            }
        }

        public LootTable.Builder multiBlockCondition(MultiBlock block) {
            StatePropertiesPredicate.Builder properties = StatePropertiesPredicate.Builder.properties();
            HashMap<Property<?>, Comparable<?>> propertyMap = new HashMap<>();

            block.addMainBlockProperties(propertyMap);

            propertyMap.forEach((property, comparable) ->
                    properties.hasProperty(property, comparable.toString()));

            return LootTable.lootTable()
                    .withPool(applyExplosionCondition(block, LootPool.lootPool()
                            .setRolls(ConstantIntValue.exactly(1))
                            .add(LootItem.lootTableItem(block)
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                            .setProperties(properties)))));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return BlockInit.BLOCKS.getEntries().stream()
                    .map(RegistryObject::get)
                    .filter(block -> !skipLootTable(block))
                    .collect(Collectors.toList());
        }
    }
}
