package com.duckblade.osrs.sailing.features.barracudatrials;

import com.duckblade.osrs.sailing.module.PluginLifecycleComponent;
import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.Perspective;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;

public class BarracudaTrialRouteTool extends Overlay implements PluginLifecycleComponent
{
	private final Client client;

	private List<WorldPoint> points = new ArrayList<>();

	@Inject
	BarracudaTrialRouteTool(Client client)
	{
		this.client = client;

		setPosition(OverlayPosition.DYNAMIC);
	}

	private void reset()
	{
		points.clear();
			;
	}

	private BarracudaTrialRouteTool pt(int x, int y)
	{
		this.points.add(new WorldPoint(x, y, 0));
		return this;
	}

	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		MenuAction menuAction = event.getMenuEntry().getType();
		final boolean hotKeyPressed = client.isKeyPressed(KeyCode.KC_SHIFT) || points.size() > 0;
		if (hotKeyPressed && (menuAction == MenuAction.WALK || menuAction == MenuAction.SET_HEADING))
		{
			WorldView wv = client.getTopLevelWorldView();
			var st = wv.getSelectedSceneTile();
			if (st == null)
			{
				return;
			}
			var wl = st.getWorldLocation();

			client.createMenuEntry(-1)
				.setOption("Boat Point")
				.setTarget(wl.getX() + ", " + wl.getY())
				.setType(MenuAction.RUNELITE)
				.onClick(e ->
				{
					points.add(wl);

					var out = "new BoatPath.Builder()\n";
					for (var pt : points)
					{
						out += "\t.pt(" + pt.getX() + ", " + pt.getY() + ")\n";
					}
					out += "\t.build()";
					System.out.println("\n" + out);
				});
		}
	}

	@Subscribe
	private void onCommandExecuted(CommandExecuted ev)
	{
		switch (ev.getCommand())
		{
			case "btr":
				reset();
				break;
			case "btpop":
				points.remove(points.size() - 1);
				break;
		}
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		g.setColor(Color.GREEN.darker());
		g.setStroke(new BasicStroke(2));

		if (points.size() >= 2)
		{
			var path = new ArrayList<BoatPath.Point>();
			for (var wp : points)
			{
				path.add(new BoatPath.Point(wp.getX(), wp.getY()));
			}
			new BoatPath(List.of(path)).render(client, g, 0, path.get(path.size() - 1).end);
		}

		if (points.size() > 0)
		{
			WorldView wv = client.getTopLevelWorldView();
			var st = wv.getSelectedSceneTile();
			if (st != null)
			{
				var wl = st.getWorldLocation();
				var start = points.get(points.size() - 1);

				double dx = wl.getX() - start.getX();
				double dy = wl.getY() - start.getY();

				double radial = 16. / (Math.PI * 2.);
				double angle = Math.round(Math.atan2(dy, dx) * radial) / radial;
				double radius = Math.hypot(dx, dy);

				int x2 = start.getX() + (int) Math.round(radius * Math.cos(angle));
				int y2 = start.getY() + (int) Math.round(radius * Math.sin(angle));

				g.setColor(Color.BLACK);

				var lpa = LocalPoint.fromWorld(wv, start);
				var lpb = LocalPoint.fromWorld(wv, x2, y2);
				if (lpa != null && lpb != null)
				{
					var a = Perspective.localToCanvas(client, lpa, 0);
					var b = Perspective.localToCanvas(client, lpb, 0);

					if (a != null && b != null)
					{
						g.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
					}

					var p = Perspective.getCanvasTilePoly(client, lpb, 0);
					if (p != null)
					{
						g.draw(p);
					}
				}
			}
		}

		return null;
	}
}
