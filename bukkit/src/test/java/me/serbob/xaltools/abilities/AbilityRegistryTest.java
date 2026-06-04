package me.serbob.xaltools.abilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbilityRegistryTest {

    @Test
    void getInstance_returnsSameInstance() {
        AbilityRegistry first = AbilityRegistry.getInstance();
        AbilityRegistry second = AbilityRegistry.getInstance();
        assertSame(first, second);
    }
}
