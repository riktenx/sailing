package com.duckblade.osrs.sailing.features.barracudatrials;

import com.duckblade.osrs.sailing.module.PluginLifecycleComponent;
import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Perspective;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;

public class BarracudaTrialRouteTool extends Overlay implements PluginLifecycleComponent
{
	private final Client client;

	private List<Object> points = new ArrayList<>();

	private Point2D.Float hoveredPoint;

	@Inject
	BarracudaTrialRouteTool(Client client)
	{
		this.client = client;

		setPosition(OverlayPosition.DYNAMIC);
	}

	private void reset()
	{
		points.clear();

	}

	private BarracudaTrialRouteTool pt(float x, float y)
	{
		this.points.add(new Point2D.Float(x, y));
		return this;
	}

	private BarracudaTrialRouteTool crate(int obj)
	{
		this.points.add(obj);
		return this;
	}

	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		MenuAction menuAction = event.getMenuEntry().getType();
		final boolean active = points.size() > 0;
		if (active && (menuAction == MenuAction.WALK || menuAction == MenuAction.SET_HEADING))
		{
			WorldView wv = client.getTopLevelWorldView();
			var st = wv.getSelectedSceneTile();
			if (st == null)
			{
				return;
			}
			var wl = st.getWorldLocation();

			if (hoveredPoint != null)
			{
				client.createMenuEntry(-1)
					.setOption("Boat Point")
					.setTarget(hoveredPoint.x + ", " + hoveredPoint.y)
					.setType(MenuAction.RUNELITE)
					.onClick(e ->
					{
						points.add(hoveredPoint);
						print();
					});
			}
		}
		if (active && menuAction == MenuAction.EXAMINE_OBJECT)
		{
			// these only have examine on them, and examine uses the transformed id
			// which we don't want ;_;

			var me = event.getMenuEntry();
			var mewv = client.getWorldView(me.getWorldViewId());
			for (var go : mewv.getScene().getTiles()[mewv.getPlane()][me.getParam0()][me.getParam1()].getGameObjects())
			{
				if (go != null)
				{
					var id = go.getId();
					var multilocs = client.getObjectDefinition(id).getImpostorIds();
					if (multilocs != null && IntStream.of(multilocs).anyMatch(i -> i == me.getIdentifier()))
					{
						client.createMenuEntry(-1)
							.setOption("Mark Crate")
							.setTarget("" + id)
							.setType(MenuAction.RUNELITE)
							.onClick(e ->
							{
								points.add(id);
								print();
							});
					}
				}
			}
		}
	}

	@SneakyThrows
	private void print()
	{
		var out = "new Trial.Builder()\n";
		for (var obj : points)
		{
			if (obj instanceof Point2D.Float)
			{
				var pt = (Point2D.Float) obj;
				out += "\t.pt(" + pt.getX() + "f, " + pt.getY() + "f)\n";
			}
			else if (obj instanceof Integer)
			{
				int id = (Integer) obj;
				String name = "" + id;
				for (var f : ObjectID.class.getFields())
				{
					if (Modifier.isStatic(f.getModifiers()) && f.getType() == int.class)
					{
						f.setAccessible(true);
						if ((Integer) f.get(null) == id)
						{
							name = "ObjectID." + f.getName();
						}
					}
				}

				out += "\t.crate(" + name + ")\n";
			}
		}
		out += "\t.finish()\n";
		System.out.println("\n" + out);
	}

	@Subscribe
	private void onCommandExecuted(CommandExecuted ev)
	{
		switch (ev.getCommand())
		{
			case "btpush":
				var wv = client.getTopLevelWorldView();
				var lp = wv.worldEntities()
					.byIndex(client.getLocalPlayer().getWorldView().getId())
					.getTargetLocation();
				points.add(new Point2D.Float((lp.getX() / 128f) + wv.getBaseX(), (lp.getY() / 128.f) + wv.getBaseY()));
				break;
			case "btreset":
			case "btr":
				reset();
				break;
			case "btshift":
			case "bts":
				Point2D.Float lastPt = null;
				int lastPtI = -1;
				for (int i = points.size() - 1; i >= 0; i--)
				{
					var obj = points.get(i);
					if (obj instanceof Point2D.Float)
					{
						if (lastPt == null)
						{
							lastPtI = i;
							lastPt = (Point2D.Float) obj;
						}
						else
						{
							int stepAdj = Integer.parseInt(ev.getArguments()[0]);
							points.set(lastPtI, pointOnLine((Point2D.Float) obj, lastPt, stepAdj));
							break;
						}
					}
				}
				break;
			case "btcrate":
				points.add(Integer.parseInt(ev.getArguments()[0]));
				break;
			case "btpop":
				points.remove(points.size() - 1);
				print();
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
			for (var obj : points)
			{
				if (obj instanceof Point2D.Float)
				{
					var pt = (Point2D.Float) obj;
					path.add(new BoatPath.Point(pt.x, pt.y));
				}
			}
			new BoatPath(List.of(path)).render(client, g, 0, path.get(path.size() - 1).end);
		}

		Point2D.Float lastPt = null;

		for (int i = points.size() - 1; i >= 0; i--)
		{
			var obj = points.get(i);
			if (obj instanceof Point2D.Float)
			{
				lastPt = (Point2D.Float) obj;
				break;
			}
		}

		if (lastPt != null)
		{
			WorldView wv = client.getTopLevelWorldView();
			var st = wv.getSelectedSceneTile();
			if (st != null)
			{
				var wl = st.getWorldLocation();

				hoveredPoint = pointOnLine(lastPt, new Point2D.Float(wl.getX(), wl.getY()), 0);

				var lpa = new LocalPoint((int) ((lastPt.x - wv.getBaseX()) * 128f), (int) ((lastPt.y - wv.getBaseY()) * 128f), -1);
				var lpb = new LocalPoint((int) ((hoveredPoint.x - wv.getBaseX()) * 128f), (int) ((hoveredPoint.y - wv.getBaseY()) * 128f), -1);
				var a = Perspective.localToCanvas(client, lpa, 0);
				var b = Perspective.localToCanvas(client, lpb, 0);

				g.setColor(Color.BLACK);
				if (a != null && b != null)
				{
					g.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
				}
			}
		}

		return null;
	}

	Point2D.Float pointOnLine(Point2D.Float start, Point2D.Float end, int stepAdj)
	{
		double dx = end.getX() - start.getX();
		double dy = end.getY() - start.getY();

		double radialStep = 16. / (Math.PI * 2.);
		int radial = (int) Math.round(Math.atan2(dy, dx) * radialStep);
		double angle = radial / radialStep;
		double radius = Math.hypot(dx, dy);

		double step;
		switch (radial & 3)
		{
			default:
			case 0:
				step = Math.hypot(0, .25);
				break;
			case 3:
			case 1:
				step = Math.hypot(.25, .5);
				break;
			case 2:
				step = Math.hypot(.25, .25);
				break;
		}

		radius = (Math.round(radius / step) + stepAdj) * step;

		float x2 = Math.round((start.getX() + radius * Math.cos(angle)) * 4) / 4f;
		float y2 = Math.round((start.getY() + radius * Math.sin(angle)) * 4) / 4f;

		return new Point2D.Float(x2, y2);
	}
}
