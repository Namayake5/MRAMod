package regulararmy.core;

import net.minecraft.block.state.IBlockState;
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

public class ItemLetterOfProclamation extends Item {

	public ItemLetterOfProclamation() {
		super();
		this.setCreativeTab(CreativeTabs.COMBAT);
	}
@Override
public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
	if(!worldIn.isRemote){
		IBlockState state=worldIn.getBlockState(pos);
		if(state.getBlock()==MRACore.blockBase){
			if(MRACore.leadersNum>32){
				player.sendMessage(new TextComponentString("Too many bases!"));
			}
			RegularArmyLeader theLeader=new RegularArmyLeader(worldIn,pos,(byte) ++MRACore.leadersNum);
			MRACore.leaders[MRACore.leadersNum]=theLeader;
			player.getActiveItemStack().splitStack(1);
			worldIn.setBlockState(pos,MRACore.blockBaseActive.getDefaultState());
			System.out.println(MRACore.leadersNum);
			theLeader.onStart();
			return EnumActionResult.SUCCESS;

		}
	}
	return EnumActionResult.SUCCESS;
	}
}
