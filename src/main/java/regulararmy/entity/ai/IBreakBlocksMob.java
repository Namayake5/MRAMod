package regulararmy.entity.ai;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBreakBlocksMob {

	float getblockStrength(IBlockState block, World world, BlockPos pos);
}
