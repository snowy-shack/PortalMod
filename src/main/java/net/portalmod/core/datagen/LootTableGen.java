package net.portalmod.core.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.item.Items;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.BlockStateProperty;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.portalmod.common.blocks.ForestCakeBlock;
import net.portalmod.common.blocks.MultiBlock;
import net.portalmod.common.sorted.fizzler.FizzlerFieldBlock;
import net.portalmod.common.sorted.gel.AbstractGelBlock;
import net.portalmod.common.sorted.goo.GooBlock;
import net.portalmod.core.init.BlockInit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LootTableGen extends LootTableProvider {
    public LootTableGen(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
        return ImmutableList.of(Pair.of(PMBlockLootTables::new, LootParameterSets.BLOCK));
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> p_validate_1_, ValidationTracker p_validate_2_) {
    }

    public static class PMBlockLootTables extends BlockLootTables {

        @Override
        protected void addTables() {
            for (RegistryObject<Block> block : BlockInit.BLOCKS.getEntries()) {
                setLootTable(block.get());
            }
        }

        public void setLootTable(Block block) {
            if (noDrops(block) || block.asItem() == Items.AIR) {
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

        public static boolean noDrops(Block block) {
            return block instanceof FizzlerFieldBlock
                    || block instanceof GooBlock
                    || block instanceof AbstractGelBlock;
        }

        public LootTable.Builder multiBlockCondition(MultiBlock block) {
            StatePropertiesPredicate.Builder properties = StatePropertiesPredicate.Builder.properties();
            HashMap<Property<?>, Comparable<?>> propertyMap = new HashMap<>();

            block.addMainBlockProperties(propertyMap);

            propertyMap.forEach((property, comparable) ->
                    properties.hasProperty(property, comparable.toString()));

            return LootTable.lootTable()
                    .withPool(applyExplosionCondition(block, LootPool.lootPool()
                            .setRolls(ConstantRange.exactly(1))
                            .add(ItemLootEntry.lootTableItem(block)
                                    .when(BlockStateProperty.hasBlockStateProperties(block)
                                            .setProperties(properties)))));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return BlockInit.BLOCKS.getEntries().stream()
                    .map(RegistryObject::get)
                    .filter(block -> !noDrops(block))
                    .collect(Collectors.toList());
        }
    }
}
