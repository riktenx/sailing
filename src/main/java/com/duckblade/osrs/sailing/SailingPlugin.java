package com.duckblade.osrs.sailing;

import com.google.inject.Provides;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Sailing"
)
public class SailingPlugin extends Plugin
{

	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SailingConfig config;

	@Inject
	private LuffOverlay luffOverlay;

	@Inject
	private RapidsOverlay rapidsOverlay;

	@Inject
	private SeaChartOverlay seaChartOverlay;

	@Inject
	private BoatTracker boatTracker;

	@Getter(AccessLevel.PACKAGE)
	private final Map<Integer, GameObject> cargoHolds = new HashMap<>();

	@Override
	protected void startUp() throws Exception
	{
		eventBus.register(boatTracker);

		seaChartOverlay.startUp();
		eventBus.register(seaChartOverlay);
		overlayManager.add(seaChartOverlay);

		eventBus.register(luffOverlay);
		overlayManager.add(luffOverlay);

		eventBus.register(rapidsOverlay);
		overlayManager.add(rapidsOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(rapidsOverlay);
		eventBus.unregister(rapidsOverlay);
		rapidsOverlay.shutDown();

		overlayManager.remove(luffOverlay);
		eventBus.unregister(luffOverlay);

		overlayManager.remove(seaChartOverlay);
		eventBus.unregister(seaChartOverlay);
		seaChartOverlay.shutDown();

		cargoHolds.clear();

		eventBus.unregister(boatTracker);
		boatTracker.shutDown();
	}

	@Provides
	SailingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SailingConfig.class);
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e)
	{
		if (client.getLocalPlayer().getWorldView().isTopLevel())
		{
			// not in a boat
			return;
		}

		if (!config.disableSailsWhenNotAtHelm())
		{
			return;
		}

		// todo magic constant
		// todo getSailingFacility
		if (client.getVarbitValue(VarbitID.SAILING_BOAT_FACILITY_LOCKEDIN) == 3)
		{
			// at sails
			return;
		}

		// todo magic constant
		if (e.getTarget().equals("<col=ffff>Sails"))
		{
			e.getMenuEntry().setDeprioritized(true);
		}
	}
}
