package regulararmy.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import regulararmy.entity.EntityRegularArmy;

public class EntityAISwimming extends EntityAIBase
{
    private EntityLiving theEntity;

    public EntityAISwimming(EntityRegularArmy par1EntityLiving)
    {
        this.theEntity = par1EntityLiving;
        this.setMutexBits(4);
        par1EntityLiving.getANavigator().setCanSwim(true);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        return this.theEntity.isInWater() || this.theEntity.isInLava();
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        if (this.theEntity.getRNG().nextFloat() < 0.8F)
        {
            this.theEntity.getJumpHelper().setJumping();
        }
    }
}
