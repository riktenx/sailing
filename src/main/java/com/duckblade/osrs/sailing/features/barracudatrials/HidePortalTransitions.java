package com.duckblade.osrs.sailing.features.barracudatrials;

import com.duckblade.osrs.sailing.SailingConfig;
import com.duckblade.osrs.sailing.features.util.SailingUtil;
import com.duckblade.osrs.sailing.module.PluginLifecycleComponent;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class HidePortalTransitions
	implements PluginLifecycleComponent
{

	private final Client client;
	private final ClientThread clientThread;

	// fixes a jarring half-snap during course start
	private boolean wasInTrial;

	@Override
	public boolean isEnabled(SailingConfig config)
	{
		return config.barracudaHidePortalTransitions();
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		// not using onVarbitChanged due to event ordering
		wasInTrial = client.getVarbitValue(VarbitID.SAILING_BT_IN_TRIAL) != 0;
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded e)
	{
		if (!SailingUtil.isSailing(client))
		{
			return;
		}

		if (!wasInTrial)
		{
			return;
		}

		if (e.getGroupId() != InterfaceID.GOTR_OVERLAY &&
			e.getGroupId() != InterfaceID.FADE_OVERLAY)
		{
			return;
		}

		Widget w = client.getWidget(e.getGroupId(), 0);
		assert w != null;

		clientThread.invokeLater(() -> w.setHidden(true));
	}

}
