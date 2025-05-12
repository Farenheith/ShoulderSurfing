package com.github.exopandora.shouldersurfing.forge;

import com.github.exopandora.shouldersurfing.ShoulderSurfingCommon;
import com.github.exopandora.shouldersurfing.client.InputHandler;
import com.github.exopandora.shouldersurfing.client.ShoulderSurfingImpl;
import com.github.exopandora.shouldersurfing.config.Config;
import com.github.exopandora.shouldersurfing.forge.event.ClientEventHandler;
import com.github.exopandora.shouldersurfing.plugin.PluginLoader;
import fuzs.forgeconfigapiport.forge.api.v5.NeoForgeConfigRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint.DisplayTest;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.ModLoadingWarning;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mod(ShoulderSurfingCommon.MOD_ID)
public class ShoulderSurfingForge
{
	private final FMLJavaModLoadingContext modLoadingContext;
	
	public ShoulderSurfingForge(FMLJavaModLoadingContext modLoadingContext)
	{
		this.modLoadingContext = modLoadingContext;
		IEventBus modEventBus = modLoadingContext.getModEventBus();
		modEventBus.addListener(this::clientSetup);
		modEventBus.addListener(this::loadComplete);
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
		{
			NeoForgeConfigRegistry.INSTANCE.register(ShoulderSurfingCommon.MOD_ID, Type.CLIENT, Config.CLIENT_SPEC);
			modEventBus.addListener(this::registerKeyMappingsEvent);
			modEventBus.addListener(this::modConfigLoadingEvent);
			modEventBus.addListener(this::modConfigReloadingEvent);
		});
		modLoadingContext.registerExtensionPoint(DisplayTest.class, () -> new DisplayTest(() -> "ANY", (remote, isServer) -> true));
	}
	
	@SubscribeEvent
	@SuppressWarnings("UnstableApiUsage")
	public void clientSetup(FMLClientSetupEvent event)
	{
		MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::clientTickEvent);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, ClientEventHandler::movementInputUpdateEvent);
		MinecraftForge.EVENT_BUS.addListener(ClientEventHandler::computeCameraAnglesEvent);
		
		Map<String, Object> modProperties = this.modLoadingContext.getContainer().getModInfo().getModProperties();
		List<?> incompatibleModIds = (List<?>) modProperties.getOrDefault("incompatibleMods", Collections.emptyList());
		FMLLoader.getLoadingModList().getMods().stream()
			.filter(info -> incompatibleModIds.contains(info.getModId()))
			.map(ShoulderSurfingForge::createIncompatibleModWarning)
			.forEach(ModLoader.get()::addWarning);
	}
	
	@SubscribeEvent
	public void loadComplete(FMLLoadCompleteEvent event)
	{
		PluginLoader.getInstance().loadPlugins();
	}
	
	@SubscribeEvent
	public void modConfigLoadingEvent(ModConfigEvent.Loading event)
	{
		ShoulderSurfingImpl.getInstance().init();
	}
	
	@SubscribeEvent
	public void modConfigReloadingEvent(ModConfigEvent.Reloading event)
	{
		if(ShoulderSurfingCommon.MOD_ID.equals(event.getConfig().getModId()) && event.getConfig().getType() == Type.CLIENT)
		{
			Config.onConfigReload();
		}
	}
	
	@SubscribeEvent
	public void registerKeyMappingsEvent(RegisterKeyMappingsEvent event)
	{
		event.register(InputHandler.CAMERA_LEFT);
		event.register(InputHandler.CAMERA_RIGHT);
		event.register(InputHandler.CAMERA_IN);
		event.register(InputHandler.CAMERA_OUT);
		event.register(InputHandler.CAMERA_UP);
		event.register(InputHandler.CAMERA_DOWN);
		event.register(InputHandler.SWAP_SHOULDER);
		event.register(InputHandler.TOGGLE_FIRST_PERSON);
		event.register(InputHandler.TOGGLE_THIRD_PERSON_FRONT);
		event.register(InputHandler.TOGGLE_THIRD_PERSON_BACK);
		event.register(InputHandler.FREE_LOOK);
		event.register(InputHandler.TOGGLE_CAMERA_COUPLING);
	}
	
	private static ModLoadingWarning createIncompatibleModWarning(IModInfo incompatibleMod)
	{
		String translationKey = ShoulderSurfingCommon.MOD_ID + ".modloadingissue.incompatiblemod";
		String modId = incompatibleMod.getModId();
		String modVersion = incompatibleMod.getVersion().toString();
		return new ModLoadingWarning(null, ModLoadingStage.VALIDATE, translationKey, ShoulderSurfingCommon.MOD_ID, modId, modVersion);
	}
}
