package net.portalmod.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.portalmod.common.sorted.portal.PortalEntity;
import net.portalmod.core.math.Vec3;

public class PortalActiveRenderInfo extends ActiveRenderInfo {
    private boolean initialized;
    private IBlockReader level;
    private ActiveRenderInfo reference;
    
    public void setup(ClientWorld level, PortalEntity sourcePortal, PortalEntity targetPortal, float partialTicks) {
        this.level = level;
        this.initialized = true;
        
        ActiveRenderInfo reference = Minecraft.getInstance().gameRenderer.getMainCamera();
        
        Vector3f sourceNormal = new Vec3(sourcePortal.getDirection().getNormal()).to3f();
        sourceNormal.mul(.5f);
        Vector3d sourcePos = sourcePortal.getPivotPoint().add(new Vector3d(sourceNormal));

        Vector3f targetNormal = new Vec3(targetPortal.getDirection().getNormal()).to3f();
        targetNormal.mul(.5f);
        Vector3d targetPos = targetPortal.getPivotPoint().add(new Vector3d(targetNormal));
        
        
    }

    public void setup(IBlockReader level, Vector3d transform, int xVector, int zVector, BlockPos pos, BlockPos otherPos, float yRot, float xRot, float partialTicks) {
        this.initialized = true;
        this.level = level;
        this.reference = Minecraft.getInstance().gameRenderer.getMainCamera();
        this.setRotation(reference.getYRot() + yRot, reference.getXRot() + xRot);
        
        double playerX = reference.getPosition().x();
        double playerZ = reference.getPosition().z();

        Direction side = level.getBlockState(pos).getValue(BlockStateProperties.FACING);
        Direction otherSide = level.getBlockState(otherPos).getValue(BlockStateProperties.FACING);

        float yRotDeg = ((180 + (otherSide.toYRot() - side.toYRot())) % 360);
        if(yRotDeg % 180 != 0)
            yRotDeg = -yRotDeg % 360;
        yRot = yRotDeg * (float)(Math.PI / 180.0f);

        Vector3i portalNormal = level.getBlockState(pos).getValue(BlockStateProperties.FACING).getNormal();
        Vector3d portalCenter = new Vector3d(pos.getX(), pos.getY(), pos.getZ())
                .add(.5f, .5f, .5f)
                .add((float)portalNormal.getX() * .5f, (float)portalNormal.getY() * .5f, (float)portalNormal.getZ() * .5f);

        Vector3i otherPortalNormal = level.getBlockState(otherPos).getValue(BlockStateProperties.FACING).getNormal();
        Vector3d otherPortalCenter = new Vector3d(otherPos.getX(), otherPos.getY(), otherPos.getZ())
                .add(.5f, .5f, .5f)
                .add((float)otherPortalNormal.getX() * .5f, (float)otherPortalNormal.getY() * .5f, (float)otherPortalNormal.getZ() * .5f);

        double xDist = playerX - portalCenter.x();
        double zDist = playerZ - portalCenter.z();
        double dist = Math.sqrt(Math.pow(xDist, 2) + Math.pow(zDist, 2));
        double angle = Math.atan2(xDist, zDist) + yRot;

        double x = dist * Math.sin(angle) + otherPortalCenter.x();
        double y = reference.getPosition().y() + (otherPos.getY() - pos.getY());
        double z = dist * Math.cos(angle) + otherPortalCenter.z();

        this.setPosition(x, y, z);
    }
}