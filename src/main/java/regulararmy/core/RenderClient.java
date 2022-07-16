package regulararmy.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import regulararmy.entity.EntityCannon;
import regulararmy.entity.EntityCatapult;
import regulararmy.entity.EntityCreeperR;
import regulararmy.entity.EntityEngineer;
import regulararmy.entity.EntityFastZombie;
import regulararmy.entity.EntityScouter;
import regulararmy.entity.EntitySkeletonR;
import regulararmy.entity.EntitySniperSkeleton;
import regulararmy.entity.EntityStone;
import regulararmy.entity.EntityZombieLongSpearer;
import regulararmy.entity.EntityZombieSpearer;
import regulararmy.entity.model.ModelZombieLongSpearer;
import regulararmy.entity.model.ModelZombieSpearer;
import regulararmy.entity.render.RenderCannon;
import regulararmy.entity.render.RenderCatapult;
import regulararmy.entity.render.RenderCreeperR;
import regulararmy.entity.render.RenderSkeletonR;
import regulararmy.entity.render.RenderStone;
import regulararmy.entity.render.RenderZombie;
import regulararmy.entity.render.RenderZombieSpearer;

public class RenderClient extends RenderProxy{

	public void init(){
		RenderManager rm=Minecraft.getMinecraft().getRenderManager();

		RenderingRegistry.registerEntityRenderingHandler(EntitySniperSkeleton.class,new RenderSkeletonR(rm));
		RenderingRegistry.registerEntityRenderingHandler(EntitySkeletonR.class,new RenderSkeletonR(rm));
		RenderingRegistry.registerEntityRenderingHandler(EntityEngineer.class, new RenderZombie(rm));
		RenderingRegistry.registerEntityRenderingHandler(EntityScouter.class, new RenderZombie(rm));
		RenderingRegistry.registerEntityRenderingHandler(EntityFastZombie.class, new RenderZombie(rm));
		RenderingRegistry.registerEntityRenderingHandler(EntityCreeperR.class,new RenderCreeperR(rm));
		RenderingRegistry.registerEntityRenderingHandler(EntityZombieSpearer.class,new RenderZombieSpearer(rm,new ModelZombieSpearer()));
		RenderingRegistry.registerEntityRenderingHandler(EntityZombieLongSpearer.class,new RenderZombieSpearer(rm,new ModelZombieLongSpearer()));
		RenderingRegistry.registerEntityRenderingHandler(EntityCatapult.class,new RenderCatapult(rm));
		RenderingRegistry.registerEntityRenderingHandler(EntityCannon.class,new RenderCannon(rm));
		RenderingRegistry.registerEntityRenderingHandler(EntityStone.class,new RenderStone(rm));
	}

}
