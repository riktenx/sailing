package com.duckblade.osrs.sailing.features.barracudatrials;

import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;

class BoatPath
{
	float[] xs;
	float[] ys;
	float[] zs;

	int[] x2ds;
	int[] y2ds;

	private BoatPath(float[] xs, float[] ys)
	{
		this.xs = xs;
		this.ys = ys;

		zs = new float[xs.length];

		x2ds = new int[xs.length];
		y2ds = new int[xs.length];
	}

	public static class Builder
	{
		List<Point> points = new ArrayList<>();

		public Builder()
		{
		}

		public Builder pt(int x, int y)
		{
			points.add(new Point(x * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE, y * Perspective.LOCAL_TILE_SIZE + Perspective.LOCAL_HALF_TILE_SIZE));
			return this;
		}

		public BoatPath build()
		{
			float[] xs = new float[(points.size() - 2) * 4 + 2];
			float[] ys = new float[xs.length];

			int coordi = 0;

			for (int i = 0; i < points.size(); i++)
			{
				int cx = points.get(i).getX();
				int cy = points.get(i).getY();

				if (i > 0 && i < points.size() - 1)
				{
					float sz = 2;

					project(xs, ys, sz, cx, cy, xs[coordi - 1], ys[coordi - 1], coordi, coordi + 1);
					project(xs, ys, sz, cx, cy, points.get(i + 1).getX(), points.get(i + 1).getY(), coordi + 3, coordi + 2);
					coordi += 4;
				}
				else
				{
					xs[coordi] = cx;
					ys[coordi] = cy;
					coordi += 1;
				}
			}

			if(coordi != xs.length)
			{
				System.out.println(coordi + " " + xs.length);
			}

			return new BoatPath(xs, ys);
		}

		private static void project(float[] xs, float[] ys, float sz, float x0, float y0, float x1, float y1, int pt, int ctrl)
		{
			float cd = sz / 2;

			x1 -= x0;
			y1 -= y0;
			float m = 128f / (float) Math.sqrt(x1 * x1 + y1 * y1);

			xs[pt] = x0 + x1 * m * sz;
			ys[pt] = y0 + y1 * m * sz;
			xs[ctrl] = x0 + x1 * m * (sz - cd);
			ys[ctrl] = y0 + y1 * m * (sz - cd);
		}
	}

	void render(Client client, Graphics2D g)
	{
		var wv = client.getTopLevelWorldView();

		Perspective.modelToCanvas(client, wv,
			xs.length,
			wv.getBaseX() * -Perspective.LOCAL_TILE_SIZE, wv.getBaseY() * -Perspective.LOCAL_TILE_SIZE, 0,
			0,
			xs, ys, zs,
			x2ds, y2ds);

		CubicCurve2D.Float curve = new CubicCurve2D.Float();

		for (int i = 0; i < xs.length - 1; )
		{
			if (x2ds[i] != Integer.MIN_VALUE && x2ds[i + 1] != Integer.MIN_VALUE)
			{
				g.drawLine(x2ds[i], y2ds[i], x2ds[i + 1], y2ds[i + 1]);
			}
			i++;

			if (i + 3 < xs.length)
			{
				curve.x1 = x2ds[i];
				curve.y1 = y2ds[i];
				curve.ctrlx1 = x2ds[++i];
				curve.ctrly1 = y2ds[i];
				curve.ctrlx2 = x2ds[++i];
				curve.ctrly2 = y2ds[i];
				curve.x2 = x2ds[++i];
				curve.y2 = y2ds[i];
				if (curve.x1 != Integer.MIN_VALUE
					&& curve.ctrlx1 != Integer.MIN_VALUE
					&& curve.ctrlx2 != Integer.MIN_VALUE
					&& curve.x2 != Integer.MIN_VALUE)
				{
					g.draw(curve);
				}
			}
		}
	}
}
