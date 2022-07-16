package regulararmy.entity;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import regulararmy.entity.ai.EntityAIBreakBlock;
import regulararmy.entity.ai.EntityAIShareTarget;
import regulararmy.entity.ai.IBreakBlocksMob;

public class EntityEngineer extends EntityZombieR implements IBreakBlocksMob
{
   public EntityAIBreakBlock breakBlockAI=new EntityAIBreakBlock(this);
   public EntityAIShareTarget shareTargetAI=new EntityAIShareTarget(this);
    /**
     * Ticker used to determine the time remaining for this zombie to convert into a villager when cured.
     */

    public EntityEngineer(World par1World)
    {
        super(par1World);
        this.tasks.addTask(2, breakBlockAI);
        this.targetTasks.addTask(4, this.shareTargetAI);
        this.doRideHorses=false;
    }
    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2);
        //this.getAttributeMap().func_111150_b(field_110186_bp).setAttribute(this.rand.nextDouble() * ForgeDummyContainer.zombieSummonBaseChance);
    }

    /**
     * Makes entity wear random armor based on difficulty
     */
    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty)
    {
    	this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_PICKAXE));
    }


	@Override
	public float getblockStrength(IBlockState block,
			World world,BlockPos pos) {
        float hardness = block.getBlockHardness(world, pos);
        if (hardness < 0.0F)
        {
            return Float.MAX_VALUE;
        }
        ItemStack stack = this.getHeldItemMainhand();
        float f = (stack == null ? 1.0F : stack.getItem().getDestroySpeed(stack, block));

        if (f > 1.0F)
        {
            int i = EnchantmentHelper.getEfficiencyModifier(this);
            ItemStack itemstack = this.getHeldItemMainhand();

            if (i > 0 && itemstack != null)
            {
                f+= (float)(i * i + 1);
            }
        }

        if (this.isPotionActive(MobEffects.HASTE))
        {
            f *= 1.0F + (float)(this.getActivePotionEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2F;
        }

        if (this.isPotionActive(MobEffects.MINING_FATIGUE))
        {
            f *= 1.0F - (float)(this.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier() + 1) * 0.2F;
        }

        if (this.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(this))
        {
            f /= 3.0F;
        }

        if (!this.onGround)
        {
            f /= 3.0F;
        }
        return (f < 0 ? 0 : f);
	}

}
