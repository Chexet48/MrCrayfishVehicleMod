package com.mrcrayfish.vehicle.client.render;

import com.mrcrayfish.vehicle.client.EntityRaytracer;
import com.mrcrayfish.vehicle.common.entity.PartPosition;
import com.mrcrayfish.vehicle.entity.EntityPlane;
import com.mrcrayfish.vehicle.entity.VehicleProperties;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

/**
 * Author: MrCrayfish
 */
public class RenderPlaneWrapper<T extends EntityPlane & EntityRaytracer.IEntityRaytraceable, R extends AbstractRenderVehicle<T>> extends RenderVehicleWrapper<T, R>
{
    public RenderPlaneWrapper(R renderVehicle)
    {
        super(renderVehicle);
    }

    public void render(T entity, float partialTicks)
    {
        if(entity.isDead)
            return;

        GlStateManager.pushMatrix();
        {
            VehicleProperties properties = entity.getProperties();

            //Apply vehicle rotations and translations. This is applied to all other parts
            PartPosition bodyPosition = properties.getBodyPosition();
            GlStateManager.rotate((float) bodyPosition.getRotX(), 1, 0, 0);
            GlStateManager.rotate((float) bodyPosition.getRotY(), 0, 1, 0);
            GlStateManager.rotate((float) bodyPosition.getRotZ(), 0, 0, 1);

            GlStateManager.translate(0, 0.5, 0);

            float bodyPitch = entity.prevBodyRotationX + (entity.bodyRotationX - entity.prevBodyRotationX) * partialTicks;
            GlStateManager.rotate(-bodyPitch, 1, 0, 0);

            float bodyRoll = entity.prevBodyRotationZ + (entity.bodyRotationZ - entity.prevBodyRotationZ) * partialTicks;
            GlStateManager.rotate(-bodyRoll, 0, 0, 1);

            GlStateManager.translate(0, -0.5, 0);

            //Translate the body
            GlStateManager.translate(bodyPosition.getX(), bodyPosition.getY(), bodyPosition.getZ());

            //Translate the vehicle to match how it is shown in the model creator
            GlStateManager.translate(0, 0.5, 0);

            //Apply vehicle scale
            GlStateManager.translate(0, -0.5, 0);
            GlStateManager.scale(bodyPosition.getScale(), bodyPosition.getScale(), bodyPosition.getScale());
            GlStateManager.translate(0, 0.5, 0);

            //Translate the vehicle so it's axles are half way into the ground
            GlStateManager.translate(0, properties.getAxleOffset() * 0.0625F, 0);

            //Translate the vehicle so it's actually riding on it's wheels
            GlStateManager.translate(0, properties.getWheelOffset() * 0.0625F, 0);

            //Render body
            renderVehicle.render(entity, partialTicks);

            //Render the engine if the vehicle has explicitly stated it should
            if(entity.shouldRenderEngine() && entity.hasEngine())
            {
                this.renderEngine(entity, properties.getEnginePosition(), entity.engine);
            }

            //Render the fuel port of the vehicle
            if(entity.shouldRenderFuelPort() && entity.requiresFuel())
            {
                EntityRaytracer.RayTraceResultRotated result = EntityRaytracer.getContinuousInteraction();
                if (result != null && result.entityHit == entity && result.equalsContinuousInteraction(EntityRaytracer.FUNCTION_FUELING))
                {
                    this.renderPart(properties.getFuelPortPosition(), entity.fuelPortBody);
                    if(renderVehicle.shouldRenderFuelLid())
                    {
                        this.renderPart(properties.getFuelPortLidPosition(), entity.fuelPortLid);
                    }
                    entity.playFuelPortOpenSound();
                }
                else
                {
                    this.renderPart(properties.getFuelPortPosition(), entity.fuelPortClosed);
                    entity.playFuelPortCloseSound();
                }
            }


            if(entity.isKeyNeeded())
            {
                this.renderPart(properties.getKeyPortPosition(), entity.keyPort);
                if(!entity.getKeyStack().isEmpty())
                {
                    this.renderKey(properties.getKeyPosition(), entity.getKeyStack());
                }
            }
        }
        GlStateManager.popMatrix();
    }
}
