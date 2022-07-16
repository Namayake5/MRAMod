package regulararmy.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import regulararmy.entity.EntityCannon;
import regulararmy.entity.EntityCatapult;
import regulararmy.entity.EntityCreeperR;
import regulararmy.entity.EntityEngineer;
import regulararmy.entity.EntityFastZombie;
import regulararmy.entity.EntityScouter;
import regulararmy.entity.EntitySkeletonR;
import regulararmy.entity.EntitySniperSkeleton;
import regulararmy.entity.EntityStone;
import regulararmy.entity.EntityZombieLongSpearer;
import regulararmy.entity.EntityZombieSpearer;
import regulararmy.entity.command.RegularArmyLeader;

@Mod(modid="monsterregulararmy",name="monsterRegularArmy",version="ALPHA1_6")
public class MRACore {
	@Instance("monsterregulararmy")
	public static MRACore instance;

	public static String MODID="monsterregulararmy";

	public static Block blockBase;
	public static Block blockBaseActive;
	public static Block blockMonster;
	public static Item itemLetterOfProclamation;
	public static Item itemLetterOfPeace;
	public static RegularArmyLeader[] leaders=new RegularArmyLeader[256];
	public static int leadersNum=-1;
	public static List<Class> entityList=new ArrayList();
	  public static List<String> entityIDList = new ArrayList<String>();

	@SidedProxy(clientSide="regulararmy.core.RenderClient",serverSide="regulararmy.core.RenderProxy")
	public static RenderProxy proxy;

	public static Block[] blocksDoNotDrop;
	public static byte[] blocksDoNotDropMeta;
	Property blocksDoNotDropP;
	public static Item[] weapons;
	public static int[] weaponsDamage;
	public static int[] weaponsTier;
	Property weaponsAndTiersP;
	public static Item[] helms;
	public static int[] helmsDamage;
	public static int[] helmsTier;
	Property helmsAndTiersP;
	public static Item[] chests;
	public static int[] chestsDamage;
	public static int[] chestsTier;
	Property chestsAndTiersP;
	public static Item[] legs;
	public static int[] legsDamage;
	public static int[] legsTier;
	Property legsAndTiersP;
	public static Item[] boots;
	public static int[] bootsDamage;
	public static int[] bootsTier;
	Property bootsAndTiersP;

	public static Item[] results;
	public static int[] resultsDamage;
	public static int[] resultsTier;
	Property resultsAndTiersP;
	public static int spawnRange;
	public static int maxWave;
	public static int firstWave;
	public static int waveMobVanish;
	public static int maxHP;
	public static int spawnInterval;
	public static int waveInterval;
	public static int searchSpawnWaveInterval;
	public static double unitMultiplier;
	public static double creeperRushChance;

	public static boolean isBowgun;
	public static boolean isMachinebow;
	public static boolean doTargetPlayers;
	public static int maxSpawnHeight;
	public static int minSpawnHeight;
	public static boolean doDropItem;



	public static boolean logEntity=false;
	public static boolean logRegion=false;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e){

		List<MRAEntityData> dataList=new ArrayList();
		dataList.add(new MRAEntityData(EntitySniperSkeleton.class, 2f, 20f, 1.2f, 1f, 6, 6, 9999,3, "EntitySniperSkeletonMRA"));
		dataList.add(new MRAEntityData(EntitySkeletonR.class,1f,10f,1.2f,1f,0,0,9999,3, "EntitySkeletonMRA"));
		dataList.add(new MRAEntityData(EntityEngineer.class,3f,-1f,1.2f,0.4f,3,3,9999,-1,"EntityEngineerMRA"));
		dataList.add(new MRAEntityData(EntityScouter.class,1f,0f,1.2f,0f,0,0,9999,0, "EntityScouterMRA"));
		dataList.add(new MRAEntityData(EntityFastZombie.class,0.5f,1f,1.2f,1f,0,0,9999,4,"EntityFastZombieMRA"));
		dataList.add(new MRAEntityData(EntityCreeperR.class,3f,20f,1.2f,1f,10,3,30,2,"EntityCreeperMRA"));
		dataList.add(new MRAEntityData(EntityZombieSpearer.class,0.5f,3f,1.2f,1f,0,0,9999,4,"EntityZombieSpearerMRA"));
		dataList.add(new MRAEntityData(EntityZombieLongSpearer.class,0.5f,3f,1.2f,1f,0,0,9999,4,"EntityZombieLongSpearerMRA"));
		dataList.add(new MRAEntityData(EntityCatapult.class,3f,30f,1.2f,1f,15,6,40,1,"EntityCatapultMRA"));
		dataList.add(new MRAEntityData(EntityCannon.class,3f,30f,1.2f,1f,30,10,50,1,"EntityCannonMRA"));
		EntityRegistry.registerModEntity(new ResourceLocation(MODID,"EntityStoneMRA"),EntityStone.class, "EntityStoneMRA", ++MRAEntityData.nextId, MRACore.instance, 80, 1, true);

		Configuration cfg = new Configuration(e.getSuggestedConfigurationFile());
		cfg.load();

		blocksDoNotDropP= cfg.get("Monsters Config", "Blocks which do not drops when broken by engineers",
				"stone;0,grass,dirt,cobblestone,sand","write as \"NAME;META\" . you don't have to write MetaDataValue.");
		weaponsAndTiersP=cfg.get("Monsters Config", "Weapons which zombies takes and tiers",
				"wooden_axe;0-0,wooden_sword-1,iron_shovel-1,stone_sword-2,iron_sword-3,diamond_sword-4,diamond_sword-5",
				"write as \"NAME;DamageValue-TIER\" you don't have to write DamageValue \n tier:0-5 0 is common and 5 is rare. ex)iron shovel:1 diamond sword:4");
		helmsAndTiersP=cfg.get("Monsters Config", "Helms which zombies takes and tiers",
				"golden_helmet-0,leather_helmet-1,chainmail_helmet-2,iron_helmet-3,diamond_helmet-4,diamond_helmet-5",
				"write as \"NAME;DamageValue-TIER\" you don't have to write DamageValue \n tier:0-5 0 is common and 5 is rare.");
		chestsAndTiersP=cfg.get("Monsters Config", "Chestplates which zombies takes and tiers",
				"golden_chestplate-0,leather_chestplate-1,chainmail_chestplate-2,iron_chestplate-3,diamond_chestplate-4,diamond_chestplate-5",
				"write as \"NAME;DamageValue-TIER\" you don't have to write DamageValue \n tier:0-5 0 is common and 5 is rare.");
		legsAndTiersP=cfg.get("Monsters Config", "Leggings which zombies takes and tiers",
				"golden_leggings-0,leather_leggings-1,chainmail_leggings-2,iron_leggings-3,diamond_leggings-4,diamond_leggings-5",
				"write as \"NAME;DamageValue-TIER\" you don't have to write DamageValue \n tier:0-5 0 is common and 5 is rare.");
		bootsAndTiersP=cfg.get("Monsters Config", "Boots which zombies takes and tiers",
				"golden_boots-0,leather_boots-1,chainmail_boots-2,iron_boots-3,diamond_boots-4,diamond_boots-5",
				"write as \"NAME;DamageValue-TIER\" you don't have to write DamageValue \n tier:0-5 0 is common and 5 is rare.");
		resultsAndTiersP=cfg.get("System Config", "Results which the base commits and tiers",
				"iron_ingot-4,redstone-3,gold_ingot-8,diamond-12",
				"write as \"NAME;DamageValue-TIER\" you don't have to write DamageValue \n tier:0 or higher.0 is common and higher is rare. ex)iron_ingot:4 diamond:12");
		Property spawnRangeP=cfg.get("System Config","The distance between Base and monsters' SpawnPoint","40");

		Property waveMobVanishP=cfg.get("System Config","Wave after monsters vanish",2);
		Property maxHPP=cfg.get("System Config","HP of the Base. default:10000","10000");
		Property spawnIntervalP=cfg.get("System Config", "Time interval between the next monster unit spawns.(tick) default:200", 200);
		Property waveIntervalP=cfg.get("System Config", "Time interval between the next wave begins.(tick) default:600", 600);

		Property doTargetPlayersP=cfg.get("Monsters Config","Whether monsters target players or not","true");
		Property maxSpawnHeightP=cfg.get("System Config","Max height where monsters spawn",256);
		Property minSpawnHeightP=cfg.get("System Config","Min height where monsters spawn",1);
		Property doDropItemP=cfg.get("System Config","Whether monsters drop items",true);
		Property creeperRushChanceP=cfg.get("System Config", "The chance that CreeperRush happens. default:0.1", 0.1);

		Property difficultyP=cfg.get("Difficulty Config", "Choose difficulty (EASY , NORMAL , HARD , VERYHARD , INSANE , IMPOSSIBLE , CUSTOM). If it is not CUSTOM , the following settings will be OVERRIDED.", "CUSTOM");

		Property maxWaveP=cfg.get("Difficulty Config","Waves when the war ends","30");
		Property firstWaveP=cfg.get("Difficulty Config","Waves when the war starts","1");
		Property unitMultiplierP=cfg.get("Difficulty Config", "Unit multiplier. For example,when it is set to 2.0, twice many units will spawn. default:1.0", 1.0);
		Property isBowgunP=cfg.get("Difficulty Config","SniperSkeletons arrow is twice stronger and preciser","false");
		Property isMachinebowP=cfg.get("Difficulty Config","Skeletons firerate is twice faster","false");

		switch(difficultyP.getString()) {
		case "EASY":
			firstWaveP.set(1);
			maxWaveP.set(20);
			isBowgunP.set(false);
			isMachinebowP.set(false);
			unitMultiplierP.set(0.5);
			break;
		case "NORMAL":
			firstWaveP.set(1);
			maxWaveP.set(30);
			isBowgunP.set(false);
			isMachinebowP.set(false);
			unitMultiplierP.set(0.5);
			break;
		case "HARD":
			firstWaveP.set(1);
			maxWaveP.set(30);
			isBowgunP.set(false);
			isMachinebowP.set(false);
			unitMultiplierP.set(1.0);
			break;
		case "VERYHARD":
			firstWaveP.set(1);
			maxWaveP.set(40);
			isBowgunP.set(false);
			isMachinebowP.set(false);
			unitMultiplierP.set(1.5);
			break;
		case "INSANE":
			firstWaveP.set(1);
			maxWaveP.set(40);
			isBowgunP.set(true);
			isMachinebowP.set(true);
			unitMultiplierP.set(2.0);
			break;
		case "IMPOSSIBLE":
			firstWaveP.set(1);
			maxWaveP.set(40);
			isBowgunP.set(true);
			isMachinebowP.set(true);
			unitMultiplierP.set(5.0);
			break;
		}

		spawnRange=spawnRangeP.getInt();
		maxWave=maxWaveP.getInt();
		firstWave=firstWaveP.getInt();
		waveMobVanish=waveMobVanishP.getInt();
		maxHP=maxHPP.getInt();
		isBowgun=isBowgunP.getBoolean();
		isMachinebow=isMachinebowP.getBoolean();
		doTargetPlayers=doTargetPlayersP.getBoolean();
		spawnInterval=spawnIntervalP.getInt();
		waveInterval=waveIntervalP.getInt();
		unitMultiplier=unitMultiplierP.getDouble();
		maxSpawnHeight=maxSpawnHeightP.getInt();
		minSpawnHeight=minSpawnHeightP.getInt();
		doDropItem=doDropItemP.getBoolean();
		creeperRushChance=creeperRushChanceP.getDouble();



		cfg.save();

		Configuration cfgMonster=new Configuration(new File(e.getModConfigurationDirectory(),"monsterRegularArmy_Monsters.cfg"));
		cfgMonster.load();
		for(MRAEntityData data:dataList){
			Property bsw=cfgMonster.get(data.unlocalizedName, "Basic spawn weight of this entity", data.basicWeight);
			Property ctt=cfgMonster.get(data.unlocalizedName, "Number of wave that this entities spawn most", data.centreTier);
			Property mint=cfgMonster.get(data.unlocalizedName,"Number of wave that this entities spawn at least",data.minTier);
			Property maxt=cfgMonster.get(data.unlocalizedName,"Number of wave that this entities spawn at most",data.maxTier);
			Property numb=cfgMonster.get(data.unlocalizedName,"Number of unit of this monster",data.numberOfMember,
					"In case of this value is negative, these monsters are followed by other monsters");
			MRAEntityData newData=new MRAEntityData(data.entityClass,data.crowdCostPerBlock,data.fightRange,data.jumpHeight,(float)bsw.getDouble(),
					ctt.getInt(),mint.getInt(),maxt.getInt(),numb.getInt(),data.unlocalizedName);
			newData.activateThisData();
		}
		cfgMonster.save();

		blockBase=new Block(Material.IRON).setUnlocalizedName("base_block").setRegistryName(
				new ResourceLocation(MODID, "base_block")).setHardness(2f).setCreativeTab(CreativeTabs.COMBAT);
		//."monsterregulararmy:base")
		ForgeRegistries.BLOCKS.register(blockBase);
		ForgeRegistries.ITEMS.register(new ItemBlock(blockBase).setRegistryName(blockBase.getRegistryName()));
		blockBaseActive=new Block(Material.IRON).setUnlocalizedName("active_base_block").setRegistryName(
				new ResourceLocation(MODID, "active_base_block")).setHardness(-1f).setResistance(60000000f);
		//."monsterregulararmy:base")
		ForgeRegistries.BLOCKS.register(blockBaseActive);
		ForgeRegistries.ITEMS.register(new ItemBlock(blockBaseActive).setRegistryName(blockBaseActive.getRegistryName()));
		blockMonster=new BlockMonster().setUnlocalizedName("monster_scaffold").setRegistryName(
				new ResourceLocation(MODID, "monster_scaffold"));
		//.setBlockTextureName("monsterregulararmy:monster");
		ForgeRegistries.BLOCKS.register(blockMonster);
		ForgeRegistries.ITEMS.register(new ItemBlock(blockMonster).setRegistryName(blockMonster.getRegistryName()));

		itemLetterOfProclamation=new ItemLetterOfProclamation().setUnlocalizedName("letter_proclamation").setRegistryName(new ResourceLocation(MODID, "letter_proclamation"));
				//.setTextureName("monsterregulararmy:letter");
		ForgeRegistries.ITEMS.register(itemLetterOfProclamation);
		itemLetterOfPeace=new ItemLetterOfPeace().setUnlocalizedName("letter_peace").setRegistryName(new ResourceLocation(MODID, "letter_peace"));
		//.setTextureName("monsterregulararmy:letter");
ForgeRegistries.ITEMS.register(itemLetterOfPeace);

		if (e.getSide().isClient()) {
			registerModels();
		}



		RegularArmyEventHandler handler=new RegularArmyEventHandler();
		FMLCommonHandler.instance().bus().register(handler);
		MinecraftForge.EVENT_BUS.register(handler);
	}

	@SideOnly(Side.CLIENT)
	public void registerModels() {
		// モデル登録メソッド
		// フルパスで入れているのは、以下のクラスがClientOnlyのため、Serverサイドでのimport事故を防ぐ目的
		net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation(itemLetterOfProclamation, 0,
				new net.minecraft.client.renderer.block.model.ModelResourceLocation(MODID + ":letter_proclamation", "inventory"));
		net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation(itemLetterOfPeace, 0,
				new net.minecraft.client.renderer.block.model.ModelResourceLocation(MODID + ":letter_peace", "inventory"));
		net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(blockBase), 0,
				new net.minecraft.client.renderer.block.model.ModelResourceLocation(MODID + ":base_block_item_model", "inventory"));
		net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(blockBaseActive), 0,
				new net.minecraft.client.renderer.block.model.ModelResourceLocation(MODID + ":active_base_block_item_model", "inventory"));
		net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(blockMonster), 0,
				new net.minecraft.client.renderer.block.model.ModelResourceLocation(MODID + ":monster_scaffold_item_model", "inventory"));
		/*
		 * いくつかの注意事項がある
		 * 1: モデルはメタデータごとに設定しなければならない
		 * なので、0～15までメタ値があるアイテムなら16回繰り返すことになる
		 * .
		 * 2: アイテムのモデルの場合はインベントリ内での表示である"inventory"だけでよい
		 * .
		 * 3: ModelResourceLocationに渡すStringはそのままファイルパスとファイル名になる
		 * 上記の場合は、assets/modid名/models/item/crow_face.jsonとなる
		 * .
		 * 追加アイテムが多い場合など、jsonファイルをフォルダ分けしたい場合はここで設定できる
		 * 例えばMOD_ID + ":domein/crow_face"とすれば、
		 * modelsフォルダ下の「domein」フォルダの中にあるcrow_face.jsonを読み取るようになる
		 */
	}

	@EventHandler
	public void postinit(FMLPostInitializationEvent event) {
		registerBlocksFromString(blocksDoNotDropP.getString());
		registerWeaponsAndTierFromString(weaponsAndTiersP.getString());
		registerResultsAndTierFromString(resultsAndTiersP.getString());
		registerHelmsAndTierFromString(helmsAndTiersP.getString());
		registerChestsAndTierFromString(chestsAndTiersP.getString());
		registerLegsAndTierFromString(legsAndTiersP.getString());
		registerBootsAndTierFromString(bootsAndTiersP.getString());


		proxy.init();
	}

	 public void registerBlocksFromString(String s){
		 String[] elements=s.split(",");
		 Block[] blocks=new Block[elements.length];
		 byte[] metas=new byte[elements.length];
		 for(int i=0;i<elements.length;i++){
			 String[] elms2=elements[i].split(";");
			 blocks[i]=tryFindingBlock(elms2[0]);
			 if(elms2.length==1){
				 metas[i]=-1;
			 }else{
				 metas[i]=Byte.parseByte(elms2[1]);
			 }

		 }
		 blocksDoNotDrop=blocks;
		 blocksDoNotDropMeta=metas;
	 }

	 public void registerWeaponsAndTierFromString(String s){
		 String[] elements=s.split(",");
		 weapons=new Item[elements.length];
		 weaponsDamage=new int[elements.length];
		 weaponsTier=new int[elements.length];
		 for(int i=0;i<elements.length;i++){
			 String[] elms2=elements[i].split("-");
			 if(elms2.length!=2){
				 System.out.println(elements[i]+" at weapons tier config is wrong!");
			 }
			 String[] elms3=elms2[0].split(";");

			 if(elms3.length==1){
				 weapons[i]=tryFindingItem(elms2[0]);
				 weaponsDamage[i]=0;
				 weaponsTier[i]=Integer.parseInt(elms2[1]);
				 if(weapons[i]==null){
					 System.out.println("Weapons named '" +elms2[0]+"' do not exist!");
				 }
			 }else{
				 weapons[i]=tryFindingItem(elms3[0]);
				 weaponsDamage[i]=Integer.parseInt(elms3[1]);
				 weaponsTier[i]=Integer.parseInt(elms2[1]);
				 if(weapons[i]==null){
					 System.out.println("Weapons named '" +elms3[0]+"' do not exist!");
				 }
			 }

		 }
	 }

	 public void registerHelmsAndTierFromString(String s){
		 String[] elements=s.split(",");
		 helms=new Item[elements.length];
		 helmsDamage=new int[elements.length];
		 helmsTier=new int[elements.length];
		 for(int i=0;i<elements.length;i++){
			 String[] elms2=elements[i].split("-");
			 if(elms2.length!=2){
				 System.out.println(elements[i]+" at armors tier config is wrong!");
			 }
			 String[] elms3=elms2[0].split(";");

			 if(elms3.length==1){
				 helms[i]=tryFindingItem(elms2[0]);
				 helmsDamage[i]=0;
				 helmsTier[i]=Integer.parseInt(elms2[1]);
				 if(helms[i]==null){
					 System.out.println("Helmets named '" +elms2[0]+"' do not exist!");
				 }
			 }else{
				 helms[i]=tryFindingItem(elms3[0]);
				 helmsDamage[i]=Integer.parseInt(elms3[1]);
				 helmsTier[i]=Integer.parseInt(elms2[1]);
				 if(helms[i]==null){
					 System.out.println("Helmets named '" +elms3[0]+"' do not exist!");
				 }
			 }

		 }
	 }

	 public void registerChestsAndTierFromString(String s){
		 String[] elements=s.split(",");
		 chests=new Item[elements.length];
		 chestsDamage=new int[elements.length];
		 chestsTier=new int[elements.length];
		 for(int i=0;i<elements.length;i++){
			 String[] elms2=elements[i].split("-");
			 if(elms2.length!=2){
				 System.out.println(elements[i]+" at armors tier config is wrong!");
			 }
			 String[] elms3=elms2[0].split(";");

			 if(elms3.length==1){
				 chests[i]=tryFindingItem(elms2[0]);
				 chestsDamage[i]=0;
				 chestsTier[i]=Integer.parseInt(elms2[1]);
				 if(chests[i]==null){
					 System.out.println("Chestplates named '" +elms2[0]+"' do not exist!");
				 }
			 }else{
				 chests[i]=tryFindingItem(elms3[0]);
				 chestsDamage[i]=Integer.parseInt(elms3[1]);
				 chestsTier[i]=Integer.parseInt(elms2[1]);
				 if(chests[i]==null){
					 System.out.println("Chestplates named '" +elms3[0]+"' do not exist!");
				 }
			 }

		 }
	 }

	 public void registerLegsAndTierFromString(String s){
		 String[] elements=s.split(",");
		 legs=new Item[elements.length];
		 legsDamage=new int[elements.length];
		 legsTier=new int[elements.length];
		 for(int i=0;i<elements.length;i++){
			 String[] elms2=elements[i].split("-");
			 if(elms2.length!=2){
				 System.out.println(elements[i]+" at armors tier config is wrong!");
			 }
			 String[] elms3=elms2[0].split(";");

			 if(elms3.length==1){
				 legs[i]=tryFindingItem(elms2[0]);
				 legsDamage[i]=0;
				 legsTier[i]=Integer.parseInt(elms2[1]);
				 if(legs[i]==null){
					 System.out.println("Leggings named '" +elms2[0]+"' do not exist!");
				 }
			 }else{
				 legs[i]=tryFindingItem(elms3[0]);
				 legsDamage[i]=Integer.parseInt(elms3[1]);
				 legsTier[i]=Integer.parseInt(elms2[1]);
				 if(legs[i]==null){
					 System.out.println("Leggings named '" +elms3[0]+"' do not exist!");
				 }
			 }

		 }
	 }

	 public void registerBootsAndTierFromString(String s){
		 String[] elements=s.split(",");
		 boots=new Item[elements.length];
		 bootsDamage=new int[elements.length];
		 bootsTier=new int[elements.length];
		 for(int i=0;i<elements.length;i++){
			 String[] elms2=elements[i].split("-");
			 if(elms2.length!=2){
				 System.out.println(elements[i]+" at armors tier config is wrong!");
			 }
			 String[] elms3=elms2[0].split(";");

			 if(elms3.length==1){
				 boots[i]=tryFindingItem(elms2[0]);
				 bootsDamage[i]=0;
				 bootsTier[i]=Integer.parseInt(elms2[1]);
				 if(boots[i]==null){
					 System.out.println("Boots named '" +elms2[0]+"' do not exist!");
				 }
			 }else{
				 boots[i]=tryFindingItem(elms3[0]);
				 bootsDamage[i]=Integer.parseInt(elms3[1]);
				 bootsTier[i]=Integer.parseInt(elms2[1]);
				 if(boots[i]==null){
					 System.out.println("Boots named '" +elms3[0]+"' do not exist!");
				 }
			 }

		 }
	 }

	 public void registerResultsAndTierFromString(String s){
		 String[] elements=s.split(",");
		 results=new Item[elements.length];
		 resultsDamage=new int[elements.length];
		 resultsTier=new int[elements.length];
		 for(int i=0;i<elements.length;i++){
			 String[] elms2=elements[i].split("-");
			 if(elms2.length!=2){
				 System.out.println(elements[i]+" at results tier config is wrong!");
			 }
			 String[] elms3=elms2[0].split(";");

			 if(elms3.length==1){
				 results[i]=tryFindingItem(elms2[0]);
				 resultsDamage[i]=0;
				 resultsTier[i]=Integer.parseInt(elms2[1]);
				 if(results[i]==null){
					 System.out.println("Item named '" +elms2[0]+"' do not exist!");
				 }
			 }else{
				 results[i]=tryFindingItem(elms3[0]);
				 resultsDamage[i]=Integer.parseInt(elms3[1]);
				 resultsTier[i]=Integer.parseInt(elms2[1]);
				 if(results[i]==null){
					 System.out.println("Item named '" +elms3[0]+"' do not exist!");
				 }
			 }

		 }
	 }

	 public Block tryFindingBlock(String s){
		 return Block.getBlockFromName(s);
	 }

	 public Item tryFindingItem(String s){
		 Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(s));
	        if (item != null)
	        {
	            return item;
	        }

	        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(s));
	        if (block != Blocks.AIR)
	        {
	            return Item.getItemFromBlock(block);
	        }
	        return null;
	 }

}
