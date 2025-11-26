package com.duckblade.osrs.sailing.features.barracudatrials;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.geometry.Geometry;

@Getter
class Trial
{
	@Getter
	private final int dbrow;

	@Getter
	private final int tier;

	private final BoatPath boatPath;
	private final List<Checkpoint> checkpoints;

	public Trial(int dbrow, int tier, Builder builder)
	{
		this.dbrow = dbrow;
		this.tier = tier;
		this.boatPath = new BoatPath(builder.points);
		this.checkpoints = builder.checkpoints;

		for (int i = 0; i < builder.checkpoints.size(); i++)
		{
			var checkpoint = this.checkpoints.get(i);
			checkpoint.start = builder.checkpointPoints.get(i).start;
			checkpoint.end = builder.checkpointPoints.get(Math.min(i + 10, this.checkpoints.size() - 1)).end;

			List<BoatPath.Point> seg = null;
			for (var iseg : builder.points)
			{
				if (iseg.get(0).start <= checkpoint.start && iseg.get(iseg.size() - 1).end >= checkpoint.start)
				{
					seg = iseg;
					break;
				}
			}

			if (seg != null)
			{
				int start = 0;
				for (; start < seg.size(); start++)
				{
					if (seg.get(start).start >= checkpoint.start)
					{
						break;
					}
				}

				int end = start;
				for (; end < seg.size(); end++)
				{
					if (seg.get(start).end > checkpoint.end)
					{
						break;
					}
				}

				outer:
				for (int j = Math.max(start - 2, 0) + 1; j < end; j++)
				{
					var a = seg.get(j - 1);
					var b = seg.get(j);
					for (int k = j + 2; k < end; k++)
					{
						var c = seg.get(k - 1);
						var d = seg.get(k);
						if (Geometry.lineIntersectionPoint(a.x, a.y, b.x, b.y, c.x, c.y, d.x, d.y) != null)
						{
							checkpoint.end = c.start;
							break outer;
						}
					}
				}
			}
		}
	}

	@RequiredArgsConstructor
	@Getter
	public static class Checkpoint
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
			this.points.add(new ArrayList<>());
		}

		Builder pt(float x, float y)
		{
			var pts = this.points.get(points.size() - 1);
			if (pts.size() == 1)
			{
				// we have to have a checkpoint associated with the first point
				crate(-1);
			}

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
			finish();
			this.points.add(new ArrayList<>());
			return this;
		}

		Builder finish()
		{
			// we have to have a checkpoint associated with the last point
			return crate(-1);
		}
	}
}
