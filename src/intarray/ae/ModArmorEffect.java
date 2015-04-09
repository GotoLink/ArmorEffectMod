package intarray.ae;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.config.Configuration;

@Mod(modid = "ArmorEffect", name = "Armor Effect Mod", version = "$version", acceptableRemoteVersions = "*")
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
				exists = !potionID.isEmpty();
				if (exists)
				{
					String strength = config.get(name, "7. Strength " + i, "").getString();
					boolean show = config.getBoolean("8. Has Particles " + i, name, true, "If particles should display for this effect.");
					aec.addEffect(potionID, strength, show);
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
