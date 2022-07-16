package regulararmy.entity.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import regulararmy.entity.EntitySkeletonR;
import regulararmy.entity.model.ModelSkeletonR;

@SideOnly(Side.CLIENT)
public class RenderSkeletonR extends RenderBiped
{
    private static final ResourceLocation skeletonTextures = new ResourceLocation("textures/entity/skeleton/skeleton.png");

    public RenderSkeletonR(RenderManager renderManagerIn)
    {
        super(renderManagerIn,new ModelSkeletonR(), 0.5F);
    }

    protected void scaleSkeleton(EntitySkeletonR par1EntitySkeleton, float par2)
    {

    }

    protected void func_82422_c()
    {
        GL11.glTranslatef(0.09375F, 0.1875F, 0.0F);
    }

    protected ResourceLocation getEntityTexture(EntitySkeletonR par1EntitySkeleton)
    {
        return skeletonTextures;
    }

    protected ResourceLocation getEntityTexture(EntityLiving p_110775_1_)
    {
        return this.getEntityTexture((EntitySkeletonR)p_110775_1_);
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    protected void preRenderCallback(EntityLivingBase par1EntityLivingBase, float par2)
    {
        this.scaleSkeleton((EntitySkeletonR)par1EntityLivingBase, par2);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(Entity par1Entity)
    {
        return this.getEntityTexture((EntitySkeletonR)par1Entity);
    }
}
