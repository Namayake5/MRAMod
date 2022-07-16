package regulararmy.core;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import regulararmy.entity.command.RegularArmyLeader;

public class ItemLetterOfPeace extends Item {

  public ItemLetterOfPeace() {
		super();
		this.setCreativeTab(CreativeTabs.COMBAT);
  }

  public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
    if (!worldIn.isRemote &&
    		worldIn.getBlockState(pos).getBlock() == MRACore.blockBaseActive) {
      for (int i = 0; i < MRACore.leaders.length; i++) {
        RegularArmyLeader leader = MRACore.leaders[i];
        if (leader != null && leader.x == pos.getX() && leader.y == pos.getY() && leader.z == pos.getZ()) {
          leader.onEnd();
          player.sendMessage(new TextComponentString("The war has ended.At wave " + leader.wave));
          worldIn.setBlockState(pos, MRACore.blockBase.getDefaultState());
          player.getHeldItem(hand).splitStack(1);
          return EnumActionResult.SUCCESS;
        }
      }
      player.sendMessage(new TextComponentString("Error! Did you replaced this block with an external tool or deleted \"MRAData\" file?"));
      worldIn.setBlockState(pos, MRACore.blockBase.getDefaultState());
      player.getHeldItem(hand).splitStack(1);
      return EnumActionResult.SUCCESS;
    }
    return EnumActionResult.PASS;
  }
}


