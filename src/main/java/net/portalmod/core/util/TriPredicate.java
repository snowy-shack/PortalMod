package net.portalmod.core.util;

/** A predicate with three inputs (replaces Forge's TriPredicate). */
@FunctionalInterface
public interface TriPredicate<A, B, C> {
    boolean test(A a, B b, C c);
}
