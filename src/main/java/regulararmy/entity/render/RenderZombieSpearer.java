package regulararmy.entity.render;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import regulararmy.entity.EntityZombieSpearer;

@SideOnly(Side.CLIENT)
public class RenderZombieSpearer extends RenderBiped
{
    private static final ResourceLocation[] textures = {new ResourceLocation("monsterregulararmy:textures/entity/spearer_bamboo.png"),
    	new ResourceLocation("monsterregulararmy:textures/entity/spearer_stone.png"),
    	new ResourceLocation("monsterregulararmy:textures/entity/spearer_iron.png"),
    	new ResourceLocation("monsterregulararmy:textures/entity/spearer_diamond.png")};

    public RenderZombieSpearer(RenderManager renderManagerIn,ModelBiped model)
    {
        super(renderManagerIn,model, 0.5F);
    }


    protected ResourceLocation getEntityTexture(EntityZombieSpearer par1EntitySkeleton)
    {
        return textures[par1EntitySkeleton.getSpearType()];
    }

    protected ResourceLocation getEntityTexture(EntityLiving p_110775_1_)
    {
        return this.getEntityTexture((EntityZombieSpearer)p_110775_1_);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(Entity par1Entity)
    {
        return this.getEntityTexture((EntityZombieSpearer)par1Entity);
    }
}
