/*
 * Copyright (c) 2011 Alan McLachlan
 *
 * This file is part of Escape From The Maze.
 *
 * Escape From The Maze is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mclachlan.maze.editor.swing;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import mclachlan.crusader.Texture;
import mclachlan.maze.data.Database;

/**
 * Animated preview for a {@link mclachlan.maze.data.MazeTexture}: frame cycling,
 * optional scroll behaviour, and a strip of per-frame thumbnails.
 */
public class TexturePreviewPanel extends JPanel
{
	private static final int PREVIEW_SIZE = 512;
	private static final int THUMB_SIZE = 48;
	private static final int TIMER_DELAY_MS = 50;

	private final AnimatedPreview animatedPreview = new AnimatedPreview();
	private final FrameStrip frameStrip = new FrameStrip();

	private javax.swing.Timer animationTimer;
	private List<BufferedImage> frames = List.of();
	private int animationDelay = -1;
	private Texture.ScrollBehaviour scrollBehaviour = Texture.ScrollBehaviour.NONE;
	private int scrollSpeed = -1;
	private int currentFrame;
	private long lastFrameChange = System.currentTimeMillis();

	/*-------------------------------------------------------------------------*/
	public TexturePreviewPanel()
	{
		super(new BorderLayout(4, 4));

		animatedPreview.setBorder(BorderFactory.createTitledBorder("Preview"));
		add(animatedPreview, BorderLayout.NORTH);
		animatedPreview.revalidate();

		JScrollPane frameScroller = new JScrollPane(
			frameStrip,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Dimension frameStripSize = new Dimension(PREVIEW_SIZE + 8, THUMB_SIZE + 12);
		frameScroller.setPreferredSize(frameStripSize);
		frameScroller.setMinimumSize(frameStripSize);
		frameScroller.setMaximumSize(frameStripSize);
		add(frameScroller, BorderLayout.SOUTH);

		animationTimer = new javax.swing.Timer(TIMER_DELAY_MS, e -> tick());
		addHierarchyListener(e ->
		{
			if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0)
			{
				if (isShowing())
				{
					start();
				}
				else
				{
					stop();
				}
			}
		});
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Dimension getPreferredSize()
	{
		Dimension preview = animatedPreview.getPreferredSize();
		Dimension frameStrip = new Dimension(PREVIEW_SIZE + 8, THUMB_SIZE + 12);
		Insets insets = getInsets();
		return new Dimension(
			Math.max(preview.width, frameStrip.width) + insets.left + insets.right,
			preview.height + frameStrip.height + 4 + insets.top + insets.bottom);
	}

	/*-------------------------------------------------------------------------*/
	@Override
	public Dimension getMaximumSize()
	{
		return getPreferredSize();
	}

	/*-------------------------------------------------------------------------*/
	public void setPreview(
		List<String> imageResources,
		int animationDelay,
		Texture.ScrollBehaviour scrollBehaviour,
		int scrollSpeed)
	{
		this.animationDelay = animationDelay;
		this.scrollBehaviour = scrollBehaviour == null
			? Texture.ScrollBehaviour.NONE
			: scrollBehaviour;
		this.scrollSpeed = scrollSpeed;
		this.currentFrame = 0;
		this.lastFrameChange = System.currentTimeMillis();
		this.frames = loadFrames(imageResources);

		frameStrip.setFrames(frames);
		animatedPreview.repaint();
		frameStrip.repaint();

		if (isShowing())
		{
			start();
		}
	}

	/*-------------------------------------------------------------------------*/
	public void clearPreview()
	{
		stop();
		frames = List.of();
		currentFrame = 0;
		animationDelay = -1;
		scrollBehaviour = Texture.ScrollBehaviour.NONE;
		scrollSpeed = -1;
		frameStrip.setFrames(frames);
		animatedPreview.repaint();
		frameStrip.repaint();
	}

	/*-------------------------------------------------------------------------*/
	public void start()
	{
		if (!animationTimer.isRunning())
		{
			animationTimer.start();
		}
	}

	/*-------------------------------------------------------------------------*/
	public void stop()
	{
		animationTimer.stop();
	}

	/*-------------------------------------------------------------------------*/
	private List<BufferedImage> loadFrames(List<String> imageResources)
	{
		List<BufferedImage> loaded = new ArrayList<>();
		if (imageResources == null)
		{
			return loaded;
		}

		for (String resource : imageResources)
		{
			if (resource == null || resource.isBlank())
			{
				continue;
			}

			BufferedImage image = Database.getInstance().getImage(resource.trim());
			if (image != null)
			{
				loaded.add(image);
			}
		}

		return loaded;
	}

	/*-------------------------------------------------------------------------*/
	private void tick()
	{
		long now = System.currentTimeMillis();

		if (animationDelay > -1
			&& frames.size() > 1
			&& now - lastFrameChange >= animationDelay)
		{
			currentFrame++;
			if (currentFrame >= frames.size())
			{
				currentFrame = 0;
			}
			lastFrameChange = now;
			frameStrip.repaint();
		}

		boolean animating = animationDelay > -1 && frames.size() > 1;
		boolean scrolling = scrollBehaviour != Texture.ScrollBehaviour.NONE && scrollSpeed > 0;
		if (animating || scrolling || !frames.isEmpty())
		{
			animatedPreview.repaint();
		}
	}

	/*-------------------------------------------------------------------------*/
	private static void paintCheckerboard(Graphics2D g2d, int x, int y, int width, int height)
	{
		final int tile = 8;
		for (int py = 0; py < height; py += tile)
		{
			for (int px = 0; px < width; px += tile)
			{
				boolean light = ((px / tile) + (py / tile)) % 2 == 0;
				g2d.setColor(light ? new Color(0xC0, 0xC0, 0xC0) : new Color(0x90, 0x90, 0x90));
				g2d.fillRect(x + px, y + py,
					Math.min(tile, width - px),
					Math.min(tile, height - py));
			}
		}
	}

	/*-------------------------------------------------------------------------*/
	private static Rectangle computeFitRect(
		int viewportX, int viewportY, int viewportW, int viewportH,
		int imageW, int imageH)
	{
		double scale = Math.min((double)viewportW / imageW, (double)viewportH / imageH);
		int drawW = Math.max(1, (int)Math.round(imageW * scale));
		int drawH = Math.max(1, (int)Math.round(imageH * scale));
		int drawX = viewportX + (viewportW - drawW) / 2;
		int drawY = viewportY + (viewportH - drawH) / 2;
		return new Rectangle(drawX, drawY, drawW, drawH);
	}

	/*-------------------------------------------------------------------------*/
	private static void drawScaledTexture(
		Graphics2D g2d,
		BufferedImage image,
		int destX,
		int destY,
		int destW,
		int destH,
		Texture.ScrollBehaviour scroll,
		int scrollSpeed,
		long timeNow)
	{
		Object oldInterpolation = g2d.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		if (imageWidth <= 0 || imageHeight <= 0)
		{
			restoreInterpolationHint(g2d, oldInterpolation);
			return;
		}

		Rectangle fit = computeFitRect(destX, destY, destW, destH, imageWidth, imageHeight);

		Shape oldClip = g2d.getClip();
		g2d.clipRect(fit.x, fit.y, fit.width, fit.height);

		if (scroll == null || scroll == Texture.ScrollBehaviour.NONE || scrollSpeed <= 0)
		{
			g2d.drawImage(image, fit.x, fit.y, fit.width, fit.height, null);
			g2d.setClip(oldClip);
			restoreInterpolationHint(g2d, oldInterpolation);
			return;
		}

		double scaleX = (double)fit.width / imageWidth;
		double scaleY = (double)fit.height / imageHeight;

		int offsetX = 0;
		int offsetY = 0;
		switch (scroll)
		{
			case LEFT ->
				offsetX = (int)((timeNow / scrollSpeed) % imageWidth);
			case RIGHT ->
				offsetX = -(int)((timeNow / scrollSpeed) % imageWidth);
			case DOWN ->
				offsetY = (int)((timeNow / scrollSpeed) % imageHeight);
			case UP ->
				offsetY = -(int)((timeNow / scrollSpeed) % imageHeight);
			default ->
			{
			}
		}

		AffineTransform oldTransform = g2d.getTransform();
		g2d.translate(fit.x, fit.y);
		g2d.scale(scaleX, scaleY);
		g2d.translate(offsetX, offsetY);

		switch (scroll)
		{
			case LEFT, RIGHT ->
			{
				g2d.drawImage(image, -imageWidth, 0, null);
				g2d.drawImage(image, 0, 0, null);
				g2d.drawImage(image, imageWidth, 0, null);
			}
			case UP, DOWN ->
			{
				g2d.drawImage(image, 0, -imageHeight, null);
				g2d.drawImage(image, 0, 0, null);
				g2d.drawImage(image, 0, imageHeight, null);
			}
			default ->
				g2d.drawImage(image, 0, 0, null);
		}

		g2d.setTransform(oldTransform);
		g2d.setClip(oldClip);
		restoreInterpolationHint(g2d, oldInterpolation);
	}

	/*-------------------------------------------------------------------------*/
	private static void restoreInterpolationHint(Graphics2D g2d, Object oldInterpolation)
	{
		if (oldInterpolation != null)
		{
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
		}
	}

	/*-------------------------------------------------------------------------*/
	private class AnimatedPreview extends JPanel
	{
		AnimatedPreview()
		{
		}

		@Override
		public Dimension getPreferredSize()
		{
			Insets insets = getInsets();
			return new Dimension(
				PREVIEW_SIZE + insets.left + insets.right,
				PREVIEW_SIZE + insets.top + insets.bottom);
		}

		@Override
		public Dimension getMaximumSize()
		{
			return getPreferredSize();
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D)g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

			Insets insets = getInsets();
			int x = insets.left;
			int y = insets.top;
			int width = getWidth() - insets.left - insets.right;
			int height = getHeight() - insets.top - insets.bottom;
			paintCheckerboard(g2d, x, y, width, height);

			if (frames.isEmpty())
			{
				g2d.setColor(Color.DARK_GRAY);
				g2d.drawString("No frames", x + 8, y + height / 2);
				g2d.dispose();
				return;
			}

			int frameIndex = Math.min(currentFrame, frames.size() - 1);
			drawScaledTexture(
				g2d,
				frames.get(frameIndex),
				x,
				y,
				width,
				height,
				scrollBehaviour,
				scrollSpeed,
				System.currentTimeMillis());

			g2d.dispose();
		}
	}

	/*-------------------------------------------------------------------------*/
	private class FrameStrip extends JPanel
	{
		FrameStrip()
		{
			setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
		}

		void setFrames(List<BufferedImage> frameImages)
		{
			removeAll();
			for (int i = 0; i < frameImages.size(); i++)
			{
				add(new FrameThumb(i, frameImages.get(i)));
			}
			revalidate();
			repaint();
		}

		@Override
		public Dimension getPreferredSize()
		{
			Dimension d = super.getPreferredSize();
			d.height = Math.max(d.height, THUMB_SIZE + 8);
			return d;
		}
	}

	/*-------------------------------------------------------------------------*/
	private class FrameThumb extends JPanel
	{
		private final int index;
		private final BufferedImage image;

		FrameThumb(int index, BufferedImage image)
		{
			this.index = index;
			this.image = image;
			setPreferredSize(new Dimension(THUMB_SIZE + 4, THUMB_SIZE + 4));
			setMinimumSize(getPreferredSize());
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D)g.create();
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

			int x = 2;
			int y = 2;
			paintCheckerboard(g2d, x, y, THUMB_SIZE, THUMB_SIZE);

			if (image != null)
			{
				Rectangle fit = computeFitRect(x, y, THUMB_SIZE, THUMB_SIZE, image.getWidth(), image.getHeight());
				g2d.drawImage(image, fit.x, fit.y, fit.width, fit.height, null);
			}

			if (index == currentFrame)
			{
				g2d.setColor(new Color(0xFF, 0xA5, 0x00));
				g2d.setStroke(new BasicStroke(2f));
				g2d.drawRect(x - 1, y - 1, THUMB_SIZE + 1, THUMB_SIZE + 1);
			}
			else
			{
				g2d.setColor(Color.GRAY);
				g2d.drawRect(x, y, THUMB_SIZE - 1, THUMB_SIZE - 1);
			}

			g2d.dispose();
		}
	}
}
