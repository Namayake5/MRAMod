package regulararmy.entity;

import java.util.Calendar;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import regulararmy.core.MRACore;
import regulararmy.entity.ai.EntityAIArrowAttack;
import regulararmy.entity.ai.EntityAIAttackOnCollide;
import regulararmy.entity.ai.EntityAIEscapeFromDrown;
import regulararmy.entity.ai.EntityAIForwardBase;
import regulararmy.entity.ai.EntityAILearnedTarget;
import regulararmy.entity.ai.EntityAISwimming;

public class EntitySkeletonR extends EntityRegularArmy implements IRangedAttackMob
{
    public EntityAIArrowAttack aiArrowAttack ;
    public EntityAIAttackOnCollide aiAttackOnCollide = new EntityAIAttackOnCollide(this,  1.2D, false);

    public EntitySkeletonR(World par1World)
    {
        super(par1World);
        aiArrowAttack= new EntityAIArrowAttack(this, 1.0D, 20, MRACore.isMachinebow?30:60, 15.0F);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIEscapeFromDrown(this));
        this.tasks.addTask(4, new EntityAIForwardBase(this,1.2f));
        //this.tasks.addTask(2, new EntityAIRestrictSun(this));
        //this.tasks.addTask(3, new EntityAIFleeSun(this, 1.0D));
        //this.tasks.addTask(5, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        //this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
        this.targetTasks.addTask(3, new EntityAILearnedTarget(this,0,true));

        if (par1World != null && !par1World.isRemote)
        {
        	this.setCombatTask();
        }
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
    }

    @Nullable
    protected ResourceLocation getLootTable()
    {
        return LootTableList.ENTITIES_SKELETON;
    }

    protected SoundEvent getAmbientSound()
    {
        return SoundEvents.ENTITY_SKELETON_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
        return SoundEvents.ENTITY_SKELETON_HURT;
    }

    protected SoundEvent getDeathSound()
    {
        return SoundEvents.ENTITY_SKELETON_DEATH;
    }

    protected SoundEvent getStepSound()
    {
        return SoundEvents.ENTITY_SKELETON_STEP;
    }

    /**
     * Get this Entity's EnumCreatureAttribute
     */
    public EnumCreatureAttribute getCreatureAttribute()
    {
        return EnumCreatureAttribute.UNDEAD;
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
    	/*
        if (this.world.isDaytime() && !this.world.isRemote)
        {
            float f = this.getBrightness(1.0F);

            if (f > 0.5F && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.world.canBlockSeeTheSky(MathHelper.floor(this.posX), MathHelper.floor(this.posY), MathHelper.floor(this.posZ)))
            {
                boolean flag = true;
                ItemStack itemstack = this.getCurrentItemOrArmor(4);

                if (itemstack != null)
                {
                    if (itemstack.isItemStackDamageable())
                    {
                        itemstack.setItemDamage(itemstack.getItemDamageForDisplay() + this.rand.nextInt(2));

                        if (itemstack.getItemDamageForDisplay() >= itemstack.getMaxDamage())
                        {
                            this.renderBrokenItemStack(itemstack);
                            this.setCurrentItemOrArmor(4, (ItemStack)null);
                        }
                    }

                    flag = false;
                }

                if (flag)
                {
                    this.setFire(8);
                }
            }
        }
*/

        NBTTagCompound nbt=this.getEntityData();

        super.onLivingUpdate();
    }

    /**
     * Handles updating while being ridden by an entity
     */
    public void updateRidden()
    {
        super.updateRidden();

        if (this.getRidingEntity() instanceof EntityCreature)
        {
            EntityCreature entitycreature = (EntityCreature)this.getRidingEntity();
            this.renderYawOffset = entitycreature.renderYawOffset;
        }
    }

    /**
     * Drop 0-2 items of this living's type. @param par1 - Whether this entity has recently been hit by a player. @param
     * par2 - Level of Looting used to kill this mob.
     */
    protected void dropFewItems(boolean par1, int par2)
    {
        int j;
        int k;

        {
            j = this.rand.nextInt(1+3 + par2);

            for (k = 0; k < j; ++k)
            {
                this.dropItem(Items.ARROW, 1);
            }
        }

        j = this.rand.nextInt(3 + par2);

        for (k = 0; k < j; ++k)
        {
            this.dropItem(Items.BONE, 1);
        }
    }

    /**
     * Gives armor or weapon for entity based on given DifficultyInstance
     */
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty)
    {
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata)
    {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        this.setEquipmentBasedOnDifficulty(difficulty);
        this.setEnchantmentBasedOnDifficulty(difficulty);
        this.setCombatTask();
        this.setCanPickUpLoot(this.rand.nextFloat() < 0.55F * difficulty.getClampedAdditionalDifficulty());

        if (this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty())
        {
            Calendar calendar = this.world.getCurrentDate();

            if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.rand.nextFloat() < 0.25F)
            {
                this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(this.rand.nextFloat() < 0.1F ? Blocks.LIT_PUMPKIN : Blocks.PUMPKIN));
                this.inventoryArmorDropChances[EntityEquipmentSlot.HEAD.getIndex()] = 0.0F;
            }
        }

        return livingdata;
    }

    /**
     * sets this entity's combat AI.
     */
    public void setCombatTask()
    {
        if (this.world != null && !this.world.isRemote)
        {
            this.tasks.removeTask(this.aiAttackOnCollide);
            this.tasks.removeTask(this.aiArrowAttack);
            ItemStack itemstack = this.getHeldItemMainhand();

            if (itemstack.getItem() instanceof net.minecraft.item.ItemBow)
            {
                int i = 20;

                if (this.world.getDifficulty() != EnumDifficulty.HARD)
                {
                    i = 40;
                }

                this.tasks.addTask(4, this.aiArrowAttack);
            }
            else
            {
                this.tasks.addTask(4, this.aiAttackOnCollide);
            }
        }
    }

    /**
     * Attack the specified entity using a ranged attack.
     */
    public void attackEntityWithRangedAttack(EntityLivingBase par1EntityLivingBase, float par2)
    {
        EntityArrow entityarrow =  this.getArrow(par2);
        if (this.getHeldItemMainhand().getItem() instanceof net.minecraft.item.ItemBow)
            entityarrow = ((net.minecraft.item.ItemBow) this.getHeldItemMainhand().getItem()).customizeArrow(entityarrow);
        double d1=MathHelper.sqrt((par1EntityLivingBase.posX-entityarrow.posX)*(par1EntityLivingBase.posX-entityarrow.posX)+
        		(par1EntityLivingBase.posZ-entityarrow.posZ)*(par1EntityLivingBase.posZ-entityarrow.posZ));
        double d2=par1EntityLivingBase.posY+ (double)par1EntityLivingBase.getEyeHeight()-0.1-entityarrow.posY;
        //double d3=(d2+0.025*(d2*d2+d1*d1)*0.25)/(Math.sqrt(d1*d1+d2*d2)-0.01*(d1*d1+d2*d2)*0.25);
        double d3=Math.atan2(d2, d1);
        entityarrow.shoot(par1EntityLivingBase.posX-this.posX, par1EntityLivingBase.posY-this.posY,par1EntityLivingBase.posZ-this.posZ, 2F, (float)(14 - this.world.getDifficulty().getDifficultyId() * 4));
        if(d2>0){
        	entityarrow.rotationPitch=(float) (((d3+0.07*-MathHelper.cos((float) (d3*4))+0.07)*180.0f/(float)Math.PI)+(d2*d2+d1*d1)*0.01f)+this.rand.nextFloat()*6-2;
        }else{
        	entityarrow.rotationPitch=(float) (((d3+0.03*-MathHelper.cos((float) (d3*4))+0.03)*180.0f/(float)Math.PI)+(d2*d2+d1*d1)*0.01f)+this.rand.nextFloat()*6-2;
        }
        entityarrow.motionY = (double)2.0*MathHelper.sin((float) (entityarrow.rotationPitch/180.0f*Math.PI));
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(entityarrow);
    }

    protected EntityArrow getArrow(float p_190726_1_)
    {
        EntityTippedArrow entitytippedarrow = new EntityTippedArrow(this.world, this);
        entitytippedarrow.setEnchantmentEffectsFromEntity(this, p_190726_1_);
        return entitytippedarrow;
    }
    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack)
    {
        super.setItemStackToSlot(slotIn, stack);

        if (!this.world.isRemote && slotIn == EntityEquipmentSlot.MAINHAND)
        {
            this.setCombatTask();
        }
    }

    public EntityAIArrowAttack getAIArrow(){
    	return this.aiArrowAttack;
    }
    @Override
    public void readFromNBT(NBTTagCompound nbt){
    	super.readFromNBT(nbt);
    	this.setCombatTask();
    }

	@Override
	public void setSwingingArms(boolean swingingArms) {
		// TODO 自動生成されたメソッド・スタブ

	}


}
