package regulararmy.entity;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import regulararmy.entity.ai.EntityAIAttackWithSpear;
import regulararmy.entity.ai.EntityAIForwardBase;
import regulararmy.entity.ai.EntityAILearnedTarget;
import regulararmy.entity.ai.EntityAIShareTarget;

public class EntityZombieSpearer extends EntityZombieR
{

	public float rotationLimitPerTick=20;
	public static final DataParameter<Integer> spearType=EntityDataManager.<Integer>createKey(EntityCannon.class,DataSerializers.VARINT);

	@Override
	public void onUpdate(){
		super.onUpdate();
	}

	public EntityZombieSpearer(World par1World)
	{
		super(par1World);
		this.setAttackAI();
		this.tasks.addTask(4, new EntityAIForwardBase(this,1.2f));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		//this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
		this.targetTasks.addTask(3, new EntityAILearnedTarget(this,0,true));
		this.targetTasks.addTask(4, new EntityAIShareTarget(this));
		this.getDataManager().register(spearType,0);//Type of spear
		this.setTurnLimitPerTick(20f);
	}

	public void setAttackAI(){
		this.tasks.addTask(5, new EntityAIAttackWithSpear
				(this,  1f, new Vec3d(-width/2, 0.3f, 2f), 1.5D, true));
	}

    public int getSpearType(){
    	return this.getDataManager().get(spearType);
    }

    public void setSpearType(int i){
    	this.getDataManager().set(spearType, i);
    	this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5+i*3);
    }

    @Override
    public void setEquipmentsFromTier(int tier){
    	int i = this.world.rand.nextInt(2);
        float f = this.world.getDifficulty() == EnumDifficulty.HARD ? 0.1F : 0.25F;

        if (this.world.rand.nextInt(64) < tier)
        {
            ++i;
        }

        if (this.world.rand.nextInt(64) < tier)
        {
            ++i;
        }

        if (this.world.rand.nextInt(64) < tier)
        {
        	++i;
        }

        {
        	ItemStack itemstack = this.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
        	if (itemstack == null || itemstack.isEmpty())
        	{
        		switch(i){
        		case 0:
        			this.setSpearType(0);
        			break;
        		case 1:
        			this.setSpearType(1);
        			break;
        		case 2:
        			this.setSpearType(2);
        			break;
        		case 3:
        			this.setSpearType(3);
        			break;
        			default:
        				this.setSpearType(3);
            			break;
        		}
        	}
        }
        super.setEquipmentsFromTier(tier);

    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("spear", this.getSpearType());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
       this.setSpearType(nbt.getInteger("spear"));
    }


}
