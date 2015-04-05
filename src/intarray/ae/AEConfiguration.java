package intarray.ae;

import net.minecraftforge.fml.common.registry.GameData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class AEConfiguration 
{
	static final ArrayList<AEConfiguration> configs = new ArrayList<AEConfiguration>();
	static final HashMap<String, Integer> potions = new HashMap<String, Integer>();
	
	private final String[] armor;
	private final String weapon;
	private final boolean[] numberForm = new boolean[5];
	private final ArrayList<Effect> effects;
	
	/**
	 * Sole constructor for config objects. 
	 * @param helmet The name of the ArmorMaterial the helmet should be
	 * @param chestplate The name of the ArmorMaterial the chestplate should be
	 * @param leggings The name of the ArmorMaterial the leggings should be
	 * @param boots The name of the ArmorMaterial the boots should be
	 * @param heldItem The name of the ToolMaterial the player's held item should be
	 */
	public AEConfiguration(String helmet, String chestplate, String leggings, String boots, String heldItem)
	{
		armor = new String[]{boots, leggings, chestplate, helmet};
		weapon = heldItem;
		effects = new ArrayList<Effect>();
		for (int i = 0; i < 5; i++)
		{
			try
			{
				int k = Integer.parseInt(i == 4 ? weapon : armor[i]);
				numberForm[i] = GameData.getItemRegistry().getObjectById(k)!=null;
			}
			catch(NumberFormatException e)
			{
				numberForm[i] = false;
			}
		}
	}
	
	/**
	 * Used to add an effect to the configuration
	 * @param id The potion id
	 * @param strength The strength, as a String. If Integer.parseInt() throws an exception, it defaults to 1. This is the value the player sees, so it starts at 1.
	 */
	public void addEffect(String id, String strength, boolean hasEffect)
	{
		int i;
		try
		{
			i = Integer.parseInt(strength);
		}
		catch(NumberFormatException e)
		{
			System.out.println("Couldn't parse given text as potion strength: " + strength);
			i = 1;
		}
		effects.add(new Effect(id.replace(" ", "").toLowerCase(), i - 1, hasEffect));
	}
	
	private boolean matches(ItemStack[] items)
	{
		for (int i = 0; i < armor.length; i++)
		{
			if (armor[i].isEmpty())
			{
				continue;
			}
			else if (armor[i].equalsIgnoreCase("EMPTY")) 
			{
				if (items[i] != null)
				{
					return false;
				}
			}
			else if (items[i] == null)
			{
				//Ignore/force empty are already handled
				//If there's no item, it's wrong.
				return false;
			}
			else if (numberForm[i])
			{
				try
				{
					Item item = GameData.getItemRegistry().getObjectById(Integer.parseInt(armor[i]));
					if (item != items[i].getItem())
					{
						return false;
					}
				}
				catch(NumberFormatException e)
				{
					System.out.println("Suddenly some text is no longer parsable as a value");
					numberForm[i] = false;
				}
			}
			else if (items[i].getItem() instanceof ItemArmor)
			{
				String s = ((ItemArmor) items[i].getItem()).getArmorMaterial().name();
				if (!s.equalsIgnoreCase(armor[i]) && !armor[i].equalsIgnoreCase("ANY"))
				{
					return false;
				}
			}
			else
			{
				Item item = GameData.getItemRegistry().getObject(armor[i]);
				if (item != items[i].getItem())
				{
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean matches(ItemStack heldItem)
	{
		if (weapon.isEmpty())
		{
			return true;
		}
		
		if (weapon.equalsIgnoreCase("EMPTY")) 
		{
			return heldItem == null;
		}
		
		if (heldItem == null)
		{
			return false;
		}

		if(weapon.equalsIgnoreCase("ANY"))
		{
			return true;
		}
		
		if (numberForm[4])
		{
			try
			{
				return GameData.getItemRegistry().getObjectById(Integer.parseInt(weapon)) == heldItem.getItem();
			}
			catch(NumberFormatException e)
			{
				System.out.println("Suddenly some text is no longer parsable as a value");
				numberForm[4] = false;
			}
		}
		
		String s;
		Item item = heldItem.getItem();
		if (item instanceof ItemArmor)
		{
			s = ((ItemArmor) item).getArmorMaterial().name();
		}
		else if (item instanceof ItemTool)
		{
			s = ((ItemTool) item).getToolMaterialName();
		}
		else if (item instanceof ItemHoe)
		{
			s = ((ItemHoe) item).getMaterialName();
		}
		else if (item instanceof ItemSword)
		{
			s = ((ItemSword) item).getToolMaterialName();
		}else
		{
			return GameData.getItemRegistry().getObject(weapon) == item;
		}
		return s.equalsIgnoreCase(weapon);
	}
	
	private void applyEffects(EntityPlayer player)
	{
		for (Effect effect : effects)
		{
			effect.apply(player);
		}
	}
	
	public static String getInstructions()
	{
		String ret = "This will define a single set. Copy this with the name \"set 2\" (3, 4, 5...) to make others.\n"
				   + "For 1 through 4, put a valid material. Leave it blank to ignore that slot. Put EMPTY to require an\n"
				   + "empty slot. Put ANY for a wildcard. Putting an id is also acceptable. Valid material names are:\n";
		ArrayList<ItemArmor.ArmorMaterial> materials = new ArrayList<ItemArmor.ArmorMaterial>();
		ArrayList<String> names = new ArrayList<String>();
		for (Object item : Item.itemRegistry.getKeys())
		{
			if (item instanceof ItemArmor)
			{
				ItemArmor.ArmorMaterial material = ((ItemArmor) item).getArmorMaterial();
				if (!materials.contains(material))
				{
					materials.add(material);
				}
			}
			
			String string = null;
			if (item instanceof ItemTool)
			{
				string = ((ItemTool) item).getToolMaterialName();
			}
			if (item instanceof ItemHoe)
			{
				string = ((ItemHoe) item).getMaterialName();
			}
			if (item instanceof ItemSword)
			{
				string = ((ItemSword) item).getToolMaterialName();
			}
			if (string != null && !names.contains(string))
			{
				names.add(string);
			}
		}
		for (ItemArmor.ArmorMaterial material : materials)
		{
			ret += material.name() + "\n";
		}
		      ret += "For 5, do the same as for 1-4. Note that tools and armor use different names. The above will\n"
		    	   + "be accepted, but will only match pieces of armor. Valid tool material names are:\n";
		for (String s : names)
		{
			ret += s + "\n";
		}
		ret += "6 and 7 are a valid potionID (See below) and level\n";
		for (String s : potions.keySet())
		{
			ret += s + "\n";
		}
		ret += "You can add \"Effect 2\" (3, 4, 5...) and \"Strength 2\" (3, 4, 5...) to add multiple effects for one set.";
		return ret;
	}
	
	/**
	 * Used to add a potion to the map of potion ids. The id string will automatically have spaces removed and be put in lower case.
	 * @param id The ID string that's used to identify the potion
	 * @param potion The ID of the potion effect
	 */
	public static void addPotionID(String id, int potion)
	{
		potions.put(id.replace(" ", "").toLowerCase(Locale.ENGLISH), potion);
	}
	
	/**
	 * Used to add a configuration to the list of active configs
	 * @param aec The configuration to be added
	 */
	public static void addConfig(AEConfiguration aec)
	{
		configs.add(aec);
	}
	
	static
	{
		potions.put("speed",      Potion.moveSpeed.id);
		potions.put("slowness",   Potion.moveSlowdown.id);
		potions.put("haste",      Potion.digSpeed.id);
		potions.put("fatigue",    Potion.digSlowdown.id);
		potions.put("strength",   Potion.damageBoost.id);
		potions.put("jump",       Potion.jump.id);
		potions.put("nausea",     Potion.confusion.id);
		potions.put("regen",      Potion.regeneration.id);
		potions.put("resist",     Potion.resistance.id);
		potions.put("fireresist", Potion.fireResistance.id);
		potions.put("breath",     Potion.waterBreathing.id);
		potions.put("invisible",  Potion.invisibility.id);
		potions.put("blindness",  Potion.blindness.id);
		potions.put("vision",     Potion.nightVision.id);
		potions.put("hunger",     Potion.hunger.id);
		potions.put("weakness",   Potion.weakness.id);
		potions.put("poison",     Potion.poison.id);
		potions.put("wither",     Potion.wither.id);
	}

	public void tick(EntityPlayer player) {
		if (this.matches(player.inventory.armorInventory) && this.matches(player.getCurrentEquippedItem())) {
			this.applyEffects(player);
		}
	}
}

final class Effect
{
	private static final int ARBITRARY_SHORT_TIME = 2;
	private static final int LOW_REGEN_MIN_TIME = 50;
	private final String name;
	private final int strength;
	private final boolean show;
	private int id;
	
	Effect(String id, int strength, boolean show)
	{
		this.name = id;
		try{
			this.id = Integer.parseInt(id);
		}catch (Exception ignored){
			this.id = -1;
		}
		this.strength = strength;
		this.show = show;
	}
	
	void apply(EntityPlayer player)
	{
		int potion = id;
		if(id<0){
			if(AEConfiguration.potions.containsKey(name))
				potion = AEConfiguration.potions.get(name);
			else
				return;
		}
		if(potion>=Potion.potionTypes.length){
			return;
		}
		if (potion != Potion.regeneration.id)
		{
			player.addPotionEffect(new PotionEffect(potion, ARBITRARY_SHORT_TIME, strength, true, show));
		}
		else if (!player.isPotionActive(potion))
		{
			player.addPotionEffect(new PotionEffect(potion, LOW_REGEN_MIN_TIME, strength, true, show));
		}
	}
}
