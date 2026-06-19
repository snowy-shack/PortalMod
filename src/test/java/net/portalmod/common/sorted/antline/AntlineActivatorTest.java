package net.portalmod.common.sorted.antline;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AntlineActivatorTest {
    @Test
    void sideAwareActivationDefaultsToLegacyActivationState() {
        LegacyActivator inactive = new LegacyActivator(false);
        LegacyActivator active = new LegacyActivator(true);

        assertFalse(inactive.isAntlineActive(null, Direction.DOWN, Direction.NORTH));
        assertTrue(active.isAntlineActive(null, Direction.DOWN, Direction.NORTH));
    }

    @Test
    void implementationsCanMakeActivationDirectional() {
        DirectionalActivator activator = new DirectionalActivator(Direction.DOWN, Direction.NORTH);

        assertTrue(activator.isAntlineActive(null, Direction.DOWN, Direction.NORTH));
        assertFalse(activator.isAntlineActive(null, Direction.DOWN, Direction.SOUTH));
        assertFalse(activator.isAntlineActive(null, Direction.UP, Direction.NORTH));
    }

    private static class LegacyActivator implements AntlineActivator {
        private final boolean active;

        private LegacyActivator(boolean active) {
            this.active = active;
        }

        @Override
        public boolean isAntlineActive(BlockState state) {
            return active;
        }

        @Override
        public Direction getHorsedOn(BlockState state) {
            return Direction.DOWN;
        }

        @Override
        public boolean antlineConnectsInDirection(Direction direction, BlockState state) {
            return true;
        }
    }

    private static class DirectionalActivator extends LegacyActivator {
        private final Direction activeSide;
        private final Direction activeConnection;

        private DirectionalActivator(Direction activeSide, Direction activeConnection) {
            super(false);
            this.activeSide = activeSide;
            this.activeConnection = activeConnection;
        }

        @Override
        public boolean isAntlineActive(BlockState state, Direction side, Direction connection) {
            return side == activeSide && connection == activeConnection;
        }
    }
}
