package regulararmy.entity;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class DummyChocolate extends EntityPlayer
{
  public DummyChocolate(World par1World)
  {
    super(par1World, new GameProfile(new UUID(0L, 0L), "ArrgChocolate"));
  }
@Override
public boolean isSpectator() {
	return false;
}

@Override
public boolean isCreative() {
	// TODO 自動生成されたメソッド・スタブ
	return false;
}
}