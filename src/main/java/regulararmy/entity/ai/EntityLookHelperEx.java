package regulararmy.entity.ai;

import java.lang.reflect.Field;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityLookHelper;

public class EntityLookHelperEx extends EntityLookHelper {
	public Field Fentity;
	/** The amount of change that is made each update for an entity facing a direction. */
	public Field FdeltaLookYaw;
	/** The amount of change that is made each update for an entity facing a direction. */
	public Field FdeltaLookPitch;
	/** Whether or not the entity is trying to look at something. */
	public Field FisLooking;
	public Field FposX;
	public Field FposY;
	public Field FposZ;
	public EntityLookHelperEx(EntityLiving p_i1613_1_) {
		super(p_i1613_1_);
		try {
			this.Fentity=EntityLookHelper.class.getDeclaredField("entity");
			this.Fentity.setAccessible(true);
			this.FdeltaLookYaw=EntityLookHelper.class.getDeclaredField("deltaLookYaw");
			this.FdeltaLookYaw.setAccessible(true);
			this.FdeltaLookPitch=EntityLookHelper.class.getDeclaredField("deltaLookPitch");
			this.FdeltaLookPitch.setAccessible(true);
			this.FisLooking=EntityLookHelper.class.getDeclaredField("isLooking");
			this.FisLooking.setAccessible(true);
			this.FposX=EntityLookHelper.class.getDeclaredField("posX");
			this.FposX.setAccessible(true);
			this.FposY=EntityLookHelper.class.getDeclaredField("posY");
			this.FposY.setAccessible(true);
			this.FposZ=EntityLookHelper.class.getDeclaredField("posZ");
			this.FposZ.setAccessible(true);
			
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

}
