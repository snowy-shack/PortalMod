package net.portalmod.core.util;

import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.logging.log4j.util.TriConsumer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.TriPredicate;

public class BlockIterator {
    private final World level;
    private final BlockPos pos;
    private BlockPos currentPos;
    private boolean result;
    
    private BlockPos tempPos;
    private BlockState tempState;
    
    public BlockIterator(World level, BlockPos pos, boolean initialResult) {
        this.level = level;
        this.pos = pos;
        this.currentPos = pos;
        this.result = initialResult;
    }
    
    private void setup(@Nullable Direction direction) {
        this.tempPos = (direction == null ? this.currentPos : this.currentPos.relative(direction));
        this.tempState = this.level.getBlockState(tempPos);
    }
    
    public BlockIterator and(@Nullable Direction direction, TriPredicate<BlockPos, BlockState, Block> predicate) {
        this.setup(direction);
        this.result &= predicate.test(this.tempPos, this.tempState, this.tempState.getBlock());
        return this;
    }
    
    public BlockIterator and(TriPredicate<BlockPos, BlockState, Block> predicate) {
        return this.and(null, predicate);
    }
    
    public BlockIterator and(@Nullable Direction direction, Predicate<Block> predicate) {
        this.setup(direction);
        this.result &= predicate.test(this.tempState.getBlock());
        return this;
    }
    
    public BlockIterator and(Predicate<Block> predicate) {
        return this.and(null, predicate);
    }
    
    public BlockIterator or(@Nullable Direction direction, TriPredicate<BlockPos, BlockState, Block> predicate) {
        this.setup(direction);
        this.result |= predicate.test(this.tempPos, this.tempState, this.tempState.getBlock());
        return this;
    }
    
    public BlockIterator or(TriPredicate<BlockPos, BlockState, Block> predicate) {
        return this.or(null, predicate);
    }
    
    public BlockIterator or(@Nullable Direction direction, Predicate<Block> predicate) {
        this.setup(direction);
        this.result |= predicate.test(this.tempState.getBlock());
        return this;
    }
    
    public BlockIterator or(Predicate<Block> predicate) {
        return this.or(null, predicate);
    }
    
    public BlockIterator exec(@Nullable Direction direction, TriConsumer<BlockPos, BlockState, Block> consumer) {
        BlockPos tempPos = (direction == null ? this.currentPos : this.currentPos.relative(direction));
        BlockState state = this.level.getBlockState(tempPos);
        consumer.accept(tempPos, state, state.getBlock());
        return this;
    }
    
    public BlockIterator move(@Nullable Direction... directionList) {
        for(Direction direction : directionList)
            if(direction != null)
                this.currentPos = this.currentPos.relative(direction);
        return this;
    }
    
    public BlockIterator resetPos() {
        this.currentPos = this.pos;
        return this;
    }
    
    public BlockIterator branchAndJoin(@Nullable Direction direction, boolean initialResult, Predicate<BlockIterator> branch) {
        BlockPos branchPos = this.currentPos;
        if(direction != null)
            branchPos = branchPos.relative(direction);
        this.result &= branch.test(new BlockIterator(level, branchPos, initialResult));
        return this;
    }
    
    public BlockIterator branch(@Nullable Direction direction, boolean initialResult, Consumer<BlockIterator> branch) {
        BlockPos branchPos = this.currentPos;
        if(direction != null)
            branchPos = branchPos.relative(direction);
        branch.accept(new BlockIterator(level, branchPos, initialResult));
        return this;
    }
    
    public BlockIterator combine(BlockIterator blockInteractor) {
        this.result &= blockInteractor.result;
        return this;
    }
    
    public BlockIterator reset() {
        this.currentPos = this.pos;
        this.result = true;
        return this;
    }
    
    public boolean then(TriConsumer<BlockPos, BlockState, Block> consumer) {
        if(!result)
            return false;
        
        BlockState state = this.level.getBlockState(currentPos);
        consumer.accept(this.currentPos, state, state.getBlock());
        return this.result;
    }
    
    public boolean thenReturn(TriPredicate<BlockPos, BlockState, Block> consumer) {
        if(!result)
            return false;
        
        BlockState state = this.level.getBlockState(currentPos);
        return consumer.test(this.currentPos, state, state.getBlock());
    }
    
    public boolean getResult() {
        return this.result;
    }
}