package fr.petitzec.summonstorm.entity.client;

import fr.petitzec.summonstorm.SummonStorm;
import fr.petitzec.summonstorm.entity.custom.FireSpirit;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FireSpiritRenderer extends MobRenderer<FireSpirit, FireSpiritModel<FireSpirit>> {
    public FireSpiritRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new FireSpiritModel<>(pContext.bakeLayer(FireSpiritModel.LAYER_LOCATION)), 0.85f);
    }

    @Override
    public ResourceLocation getTextureLocation(FireSpirit pEntity) {
        return ResourceLocation.fromNamespaceAndPath(SummonStorm.MOD_ID, "textures/entity/fire_spirit/fire_spirit.png");
    }

    @Override
    public void render(FireSpirit pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        if(pEntity.isBaby()) {
            pPoseStack.scale(0.5f, 0.5f, 0.5f);
        } else {
            pPoseStack.scale(1f, 1f, 1f);
        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }
}
