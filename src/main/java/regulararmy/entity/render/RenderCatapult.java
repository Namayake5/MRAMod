package regulararmy.entity.render;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import regulararmy.entity.model.ModelCatapult;

public class RenderCatapult extends RenderLiving {
	public static ResourceLocation textureCatapult=new ResourceLocation("monsterregulararmy:textures/entity/catapult.png");

	public RenderCatapult(RenderManager renderManagerIn) {
		super(renderManagerIn,new ModelCatapult(), 1);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
		return textureCatapult;
	}

}
