package regulararmy.core;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockMonster extends Block{

	public BlockMonster() {
		super(Material.ROCK);
		this.setCreativeTab(CreativeTabs.COMBAT);
		this.setResistance(1.0f);
		this.setTickRandomly(true);
	}

	@Override
	public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos){
		return MRACore.leadersNum==-1?0:4.0f;
	}

	@Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if(MRACore.leadersNum==-1){
			worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
		}
	}

	@Override
	public int quantityDropped(Random p_149745_1_){
		return 0;
	}

}
