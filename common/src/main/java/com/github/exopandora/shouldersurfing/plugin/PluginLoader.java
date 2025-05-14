package com.github.exopandora.shouldersurfing.plugin;

import com.github.exopandora.shouldersurfing.ShoulderSurfingCommon;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingPlugin;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;

public abstract class PluginLoader
{
	private static final PluginLoader INSTANCE = ServiceLoader.load(PluginLoader.class).findFirst().orElseThrow();
	private static final String ENTRYPOINT_KEY = "entrypoint";
	protected static final String PLUGIN_JSON_PATH = "shouldersurfing_plugin.json";
	
	public abstract void loadPlugins();
	
	protected void loadPlugin(String modName, String modId, Path path)
	{
		this.loadPlugin(new PluginContext(modName, modId, path));
	}
	
	private void loadPlugin(PluginContext context)
	{
		ShoulderSurfingCommon.LOGGER.info("Registering plugin for {}", context.formattedModName());
		
		try(Reader reader = Files.newBufferedReader(context.path()))
		{
			JsonObject configuration = JsonParser.parseReader(reader).getAsJsonObject();
			
			if(configuration.has(ENTRYPOINT_KEY))
			{
				String entrypoint = configuration.get(ENTRYPOINT_KEY).getAsString();
				IShoulderSurfingPlugin plugin = (IShoulderSurfingPlugin) Class.forName(entrypoint).getConstructor().newInstance();
				ShoulderSurfingRegistrar registrar = ShoulderSurfingRegistrar.getInstance();
				registrar.setPluginContext(context);
				plugin.register(registrar);
			}
			else
			{
				ShoulderSurfingCommon.LOGGER.error("Plugin for {} does not contain an entrypoint", context.formattedModName());
			}
		}
		catch(Throwable e)
		{
			ShoulderSurfingCommon.LOGGER.error("Failed to load plugin for {}", context.formattedModName(), e);
		}
	}
	
	protected void freeze()
	{
		ShoulderSurfingCommon.LOGGER.info("Freezing plugin registrar");
		ShoulderSurfingRegistrar.getInstance().freeze();
	}
	
	public static PluginLoader getInstance()
	{
		return INSTANCE;
	}
}
