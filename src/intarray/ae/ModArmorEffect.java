package intarray.ae;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.config.Configuration;

@Mod(modid = "ArmorEffect", name = "Armor Effect Mod", version = "$version")
public final class ModArmorEffect
{
	private Configuration config;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		config = new Configuration(event.getSuggestedConfigurationFile());
		FMLCommonHandler.instance().bus().register(this);
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		config.addCustomCategoryComment("set 1", AEConfiguration.getInstructions());
		for (int index = 1; config.hasCategory("set " + index); index++)
		{
			String name = "set " + index;
			String helmet     = config.get(name, "1. Helmet",     "").getString();
			String chestplate = config.get(name, "2. Chestplate", "").getString();
			String leggings   = config.get(name, "3. Leggings",   "").getString();
			String boots      = config.get(name, "4. Boots",      "").getString();
			String item       = config.get(name, "5. Held Item",  "").getString();
			AEConfiguration aec = new AEConfiguration(helmet, chestplate, leggings, boots, item);
			
			int i = 1;
			boolean exists;
			do
			{
				String potionID = config.get(name, "6. Potion " + i, "").getString();
				String strength = config.get(name, "7. Strength " + i, "").getString();
				exists = !potionID.isEmpty();
				if (exists)
				{
					aec.addEffect(potionID, strength);
				}
				i++;
			}
			while(exists);
			
			AEConfiguration.addConfig(aec);
		}
		if(config.hasChanged())
			config.save();
	}

	@SubscribeEvent
	public void doPotions(TickEvent.PlayerTickEvent event)
	{
		if (!event.player.worldObj.isRemote) {
			for (AEConfiguration aec : AEConfiguration.configs) {
				aec.tick(event.player);
			}
		}
	}
}
