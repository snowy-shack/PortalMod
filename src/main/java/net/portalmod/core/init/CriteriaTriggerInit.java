package net.portalmod.core.init;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.portalmod.common.triggers.CodeBoundTrigger;
import net.portalmod.common.triggers.FaithPlateElytraTrigger;
import net.portalmod.common.triggers.ShootMoonTrigger;
import net.portalmod.core.util.Registry;

public class CriteriaTriggerInit {
    private CriteriaTriggerInit() {}

    public static final Registry<AbstractCriterionTrigger<?>> REGISTRY = new Registry<>();
    
    public static final Registry.Entry<CodeBoundTrigger> SHOOT_MOON = registerCBT("shoot_moon");
    public static final Registry.Entry<CodeBoundTrigger> FAITH_PLATE_ELYTRA = registerCBT("faith_plate_elytra");
    public static final Registry.Entry<CodeBoundTrigger> PLACE_PORTALS = registerCBT("place_portals");
    public static final Registry.Entry<CodeBoundTrigger> PORTAL_SURFACE = registerCBT("portal_surface");
    public static final Registry.Entry<CodeBoundTrigger> BOUNCE_ON_GEL = registerCBT("bounce_on_gel");
    public static final Registry.Entry<CodeBoundTrigger> SHOOT_PORTAL_FAR = registerCBT("shoot_portal_far");
    public static final Registry.Entry<CodeBoundTrigger> GRAB_ENTITY = registerCBT("grab_entity");
    public static final Registry.Entry<CodeBoundTrigger> CUBE_ON_BUTTON = registerCBT("cube_on_button");

    private static <S extends AbstractCriterionTrigger<?>> S register(S trigger) {
        CriteriaTriggers.register(trigger);
        return trigger;
    }

    private static Registry.Entry<CodeBoundTrigger> registerCBT(String name) {
        return REGISTRY.register(name, () -> register(new CodeBoundTrigger(name)));
    }
}