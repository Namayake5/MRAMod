package regulararmy.entity.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import regulararmy.analysis.DataAnalyzer;
import regulararmy.analysis.FinderSettings;
import regulararmy.entity.EntityRegularArmy;

public abstract class LearningManagerBase {
	public RegularArmyLeader leader;
	
	public abstract void onStart();
	public abstract void onUpdate();
	public abstract void onEnd();
	public abstract void readFromNBT(NBTTagCompound nbt);

	public abstract NBTTagCompound writeToNBT(NBTTagCompound nbt);
	
	public abstract FinderSettings getSettings(EntityRegularArmy e);
}
