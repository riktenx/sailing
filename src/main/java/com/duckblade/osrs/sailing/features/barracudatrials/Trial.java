package com.duckblade.osrs.sailing.features.barracudatrials;

import com.duckblade.osrs.sailing.features.util.SailingUtil;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ObjectComposition;
import net.runelite.client.ui.overlay.OverlayUtil;

class Trial
{
	private final BoatPath boatPath;
	private final List<Checkpoint> checkpoints;

	public Trial(Trial.Builder builder)
	{
		this.boatPath = new BoatPath(builder.points);
		this.checkpoints = builder.checkpoints;

		for (int i = 0; i < builder.checkpoints.size(); i++)
		{
			var checkpoint = this.checkpoints.get(i);
			checkpoint.start = builder.checkpointPoints.get(i).start;
			var ee = builder.points.get(builder.points.size() - 1);
			checkpoint.end = ee.get(ee.size() - 1).end;//builder.checkpointPoints.get(Math.min(i + 30, this.checkpoints.size() - 1)).end;
			// TODO intersection check
		}
	}

	@RequiredArgsConstructor
	private static class Checkpoint
	{
		final int objectID;
		int start;
		int end;
	}

	public static class Builder
	{
		private final List<List<BoatPath.Point>> points = new ArrayList<>();
		private final List<Checkpoint> checkpoints = new ArrayList<>();
		private final List<BoatPath.Point> checkpointPoints = new ArrayList<>();

		{
			teleport();
		}

		Builder pt(float x, float y)
		{
			var pts = this.points.get(points.size() - 1);
			pts.add(new BoatPath.Point(x, y));
			return this;
		}

		Builder crate(int object)
		{
			var pts = this.points.get(this.points.size() - 1);
			this.checkpointPoints.add(pts.get(pts.size() - 1));
			this.checkpoints.add(new Checkpoint(object));
			return this;
		}

		Builder teleport()
		{
			this.points.add(new ArrayList<>());
			return this;
		}
	}

	public void render(Graphics2D g, BarracudaTrialHelper helper)
	{
		int i = 0;
		for (; i < checkpoints.size(); i++)
		{
			int obj = checkpoints.get(i).objectID;

			if (obj > -1 && helper.client.getObjectDefinition(obj).getImpostor() != null)
			{
				break;
			}
		}

		var range = checkpoints.get(Math.max(0, i - 3));

		for (; i < checkpoints.size(); i++)
		{
			var ckpt = checkpoints.get(i);
			if (ckpt.end > range.end)
			{
				break;
			}

			var obj = helper.objects.get(ckpt.objectID);
			if (obj != null)
			{
				ObjectComposition def = SailingUtil.getTransformedObject(helper.client, obj);
				if (def != null)
				{
					OverlayUtil.renderTileOverlay(g, obj, "", helper.crateColour);
				}
			}
		}

		g.setStroke(new BasicStroke(2));
		g.setColor(Color.GREEN);

		boatPath.render(helper.client, g, range.start, range.end);


	}
}
