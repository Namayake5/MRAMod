package regulararmy.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.MathHelper;

public class EntityMoveHelperEx extends EntityMoveHelper {


	public float angleMovementLimit;
	public EntityMoveHelperEx(EntityLiving p_i1614_1_,float angleMovementLimit) {
		super(p_i1614_1_);
		this.angleMovementLimit=angleMovementLimit;
	}

    public void onUpdateMoveHelper()
    {

        boolean flag=false;
        if (action == EntityMoveHelper.Action.MOVE_TO || action == EntityMoveHelper.Action.STRAFE)
        {
        	flag=true;

        }
        super.onUpdateMoveHelper();
        if(flag) {
        	 int i = MathHelper.floor(entity.getEntityBoundingBox().minY + 0.5D);
             double d0 = posX - entity.posX;
             double d1 = posZ - entity.posZ;
             double d2 = posY - (double)i;
             double d3 = d0 * d0 + d2 * d2 + d1 * d1;

             if (d3 >= 2.500000277905201E-7D)
             {
                 float f = (float)(Math.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
                 entity.rotationYaw = this.limitAngle(entity.rotationYaw, f, this.angleMovementLimit);

             }
        }
    }

    /**
     * Limits the given angle to a upper and lower limit.
     */
    public float limitAngle(float p_75639_1_, float p_75639_2_, float p_75639_3_)
    {
        float f3 = MathHelper.wrapDegrees(p_75639_2_ - p_75639_1_);

        if (f3 > p_75639_3_)
        {
            f3 = p_75639_3_;
        }

        if (f3 < -p_75639_3_)
        {
            f3 = -p_75639_3_;
        }

        return p_75639_1_ + f3;
    }

}
