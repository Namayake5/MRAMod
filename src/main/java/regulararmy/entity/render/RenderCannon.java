package regulararmy.entity.render;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import regulararmy.entity.model.ModelCannon;

public class RenderCannon extends RenderLiving {
	public static ResourceLocation textureCatapult=new ResourceLocation("monsterregulararmy:textures/entity/cannon.png");

	public RenderCannon(RenderManager renderManagerIn) {
		super(renderManagerIn,new ModelCannon(), 1);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
		return textureCatapult;
	}

}
