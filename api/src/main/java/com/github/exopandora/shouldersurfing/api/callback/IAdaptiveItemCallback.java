package com.github.exopandora.shouldersurfing.api.callback;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public interface IAdaptiveItemCallback
{
	boolean isHoldingAdaptiveItem(Minecraft minecraft, LivingEntity entity);
	
	static IAdaptiveItemCallback mainHandMatches(ItemLike... items)
	{
		return (minecraft, entity) -> containsItem(entity.getMainHandItem().getItem(), items);
	}
	
	static IAdaptiveItemCallback offHandMatches(ItemLike... items)
	{
		return (minecraft, entity) -> containsItem(entity.getOffhandItem().getItem(), items);
	}
	
	static IAdaptiveItemCallback anyHandMatches(ItemLike... items)
	{
		return (minecraft, entity) -> containsItem(entity.getMainHandItem().getItem(), items) || containsItem(entity.getOffhandItem().getItem(), items);
	}
	
	private static boolean containsItem(Item itemToFind, ItemLike... items)
	{
		for(ItemLike item : items)
		{
			if(itemToFind.equals(item.asItem()))
			{
				return true;
			}
		}
		
		return false;
	}
}
