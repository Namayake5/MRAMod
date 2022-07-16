package regulararmy.entity;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import regulararmy.entity.ai.EntityAIAttackWithSpear;

public class EntityZombieLongSpearer extends EntityZombieSpearer
{

    /**
     * Ticker used to determine the time remaining for this zombie to convert into a villager when cured.
     */

    public EntityZombieLongSpearer(World par1World)
    {
        super(par1World);
        this.setTurnLimitPerTick(10f);
    }

    @Override
    public void setAttackAI(){
		this.tasks.addTask(5, new EntityAIAttackWithSpear
				(this,  2f, new Vec3d(0, 0f, 4f), 1.5D, true));
	}
}
