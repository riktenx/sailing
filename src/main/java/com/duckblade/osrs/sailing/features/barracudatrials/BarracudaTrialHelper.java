package com.duckblade.osrs.sailing.features.barracudatrials;

import com.duckblade.osrs.sailing.SailingConfig;
import com.duckblade.osrs.sailing.features.util.SailingUtil;
import com.duckblade.osrs.sailing.module.PluginLifecycleComponent;
import com.google.common.collect.ImmutableSet;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.ObjectComposition;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.WorldViewUnloaded;
import net.runelite.api.gameval.DBTableID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Slf4j
@Singleton
public class BarracudaTrialHelper
	extends Overlay
	implements PluginLifecycleComponent
{

	private static final Set<Integer> TRACKED_OBJECTS;

	static
	{
		var trackedObjects = ImmutableSet.<Integer>builder();

		Stream.of(TrialData.values())
			.flatMap(v -> v.getObjects())
			.forEach(trackedObjects::add);

		Stream.of(JubblyJivePillars.values())
			.map(j -> j.getObject())
			.forEach(trackedObjects::add);

		trackedObjects.addAll(TrialData.UNTRACKED_CARGO);

		trackedObjects.add(
			ObjectID.SAILING_BT_JUBBLY_JIVE_TOAD_SUPPLIES_PARENT
		);

		TRACKED_OBJECTS = trackedObjects.build();
	}

	private final Client client;

	private final Map<Integer, GameObject> objects = new HashMap<>();

	private int trialDBRow = -1;
	private int trialRank;
	private boolean hasSupplyBoatItem;

	private Trial activeTrial;

	private Color crateColour;

	@Inject
	public BarracudaTrialHelper(Client client)
	{
		this.client = client;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public boolean isEnabled(SailingConfig config)
	{
		crateColour = config.barracudaHighlightLostCratesColour();
		return config.barracudaHighlightLostCrates();
	}

	@Override
	public void shutDown()
	{
		trialDBRow = -1;
		objects.clear();
	}

	@Subscribe
	public void onWorldViewUnloaded(WorldViewUnloaded e)
	{
		objects.values().removeIf(go -> go.getWorldView() == e.getWorldView());
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned e)
	{
		GameObject o = e.getGameObject();
		if (TRACKED_OBJECTS.contains(o.getId()))
		{
			objects.put(o.getId(), o);
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned e)
	{
		objects.remove(e.getGameObject().getId());
	}

	@Subscribe
	private void onScriptPreFired(ScriptPreFired ev)
	{
		if (ev.getScriptId() == 8605)
		{
			try
			{
				var args = ev.getScriptEvent().getArguments();

				trialDBRow = (Integer) args[1];
				hasSupplyBoatItem = 1 == (Integer) args[5];
				trialRank = (Integer) args[6];
			}
			catch (Exception e)
			{
				log.warn("failed to get trial args", e);
				trialDBRow = -1;
			}
		}
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		if (trialDBRow == -1)
		{
			return null;
		}

		Trial trial = null;
		if (activeTrial == null
			|| activeTrial.getDbrow() != trialDBRow
			|| activeTrial.getTier() != trialRank)
		{
			var td = TrialData.findTrial(trialDBRow, trialRank);
			if (td != null)
			{
				trial = td.buildTrial();
			}

			//TODO: activeTrial = trial;
		}

		if (trialDBRow == DBTableID.SailingBtTrialCore.Row.SAILING_BT_JUBBLY_JIVE)
		{
			renderJubbly(g);
		}

		if (trial == null)
		{
			for (GameObject o : objects.values())
			{
				if (TrialData.UNTRACKED_CARGO.contains(o.getId()))
				{
					ObjectComposition def = SailingUtil.getTransformedObject(client, o);
					if (def != null)
					{
						OverlayUtil.renderTileOverlay(g, o, "" + (o.getId() - ObjectID.SAILING_BT_JUBBLY_JIVE_COLLECTABLE_1 + 1), Color.WHITE);
					}
				}
			}

			return null;
		}

		renderTrial(trial, g);

		return null;
	}

	private void renderTrial(Trial t, Graphics2D g)
	{
		var checkpoints = t.getCheckpoints();

		int i = 0;
		for (; i < checkpoints.size(); i++)
		{
			int obj = checkpoints.get(i).objectID;

			if (obj > -1 && client.getObjectDefinition(obj).getImpostor() != null)
			{
				break;
			}
		}

		var range = checkpoints.get(Math.max(0, i - 3));

		for (; i < checkpoints.size(); i++)
		{
			var ckpt = checkpoints.get(i);
			if (ckpt.start > range.end)
			{
				break;
			}

			var obj = objects.get(ckpt.objectID);
			if (obj != null)
			{
				ObjectComposition def = SailingUtil.getTransformedObject(client, obj);
				if (def != null)
				{
					OverlayUtil.renderTileOverlay(g, obj, "", crateColour);
				}
			}
		}

		g.setStroke(new BasicStroke(2));
		g.setColor(Color.GREEN);

		t.getBoatPath().render(client, g, range.start, range.end);
	}

	private void renderJubbly(Graphics2D g)
	{
		if (!hasSupplyBoatItem)
		{
			renderInteractable(g, ObjectID.SAILING_BT_JUBBLY_JIVE_TOAD_SUPPLIES_PARENT);
			return;
		}

		boolean jubblyBefore = false;
		for (var pillar : JubblyJivePillars.values())
		{
			int state = client.getVarbitValue(pillar.getVarbit());
			if (state == 3)
			{
				jubblyBefore = true;
			}
			else if (jubblyBefore && state == 1)
			{
				renderInteractable(g, pillar.getObject());
			}
		}
	}

	private void renderInteractable(Graphics2D g, int objectID)
	{
		var obj = objects.get(objectID);
		if (obj != null)
		{
			var cb = obj.getClickbox();
			if (cb != null)
			{
				OverlayUtil.renderPolygon(g, cb, crateColour);
			}
		}
	}

}
