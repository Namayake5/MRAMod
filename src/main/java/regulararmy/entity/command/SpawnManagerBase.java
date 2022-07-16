package regulararmy.entity.command;

import net.minecraft.nbt.NBTTagCompound;

public abstract class SpawnManagerBase {
public RegularArmyLeader leader;

public SpawnManagerBase(RegularArmyLeader leader){
	this.leader=leader;
}

public abstract void onStart();

public abstract void onUpdate();

public abstract void onEnd();

public abstract void readFromNBT(NBTTagCompound nbt);

public abstract NBTTagCompound writeToNBT(NBTTagCompound nbt);
}
