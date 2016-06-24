package org.scec.vtk.timeline.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.DecimalFormat;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.opensha.commons.util.ClassUtils;
import org.scec.vtk.plugins.Plugin;
import org.scec.vtk.plugins.PluginState;
import org.scec.vtk.plugins.StatefulPlugin;
import org.scec.vtk.timeline.AnimationTimeListener;
import org.scec.vtk.timeline.KeyFrame;
import org.scec.vtk.timeline.KeyFrameList;
import org.scec.vtk.timeline.RangeAnimationListener;
import org.scec.vtk.timeline.RangeKeyFrame;
import org.scec.vtk.timeline.Timeline;
import org.scec.vtk.timeline.VisibilityKeyFrame;

import com.google.common.base.Preconditions;

public class TimelinePanel extends JPanel implements MouseListener, MouseMotionListener, AnimationTimeListener {
	
	private int pixelsPerSecond = 100;
	private double secondsPerPixel = 1d/(double)pixelsPerSecond;
	
	private double curTime;
	
	// static sizes/font for the ticks/labels
	public static final int headerHeight = 30;
	private static final double majorTickInterval = 1d;
	private static final double minorTickInterval = 0.1d;
	private static final int majorTickHeight = 12;
	private static final int minorTickHeight = 6;
	private static final DecimalFormat timeLabelFormat = new DecimalFormat("0s");
	private static final Font timeLabelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
	private static final int timeLabelLowerPad = 3;
	
	private static final Color lineColor = Color.BLACK;
	private static final Color tickColor = Color.BLACK;
	private static final Color pluginDividerColor = Color.LIGHT_GRAY;
	private static final Color tickGridColor = new Color(225, 225, 225); // null if no grid
	
	static final int heightPerPlugin = 30;
	static final int keyWidth = 5; // prefer odd for perfect alignment
	static final int keyHeight = 15; // prefer odd for perfect alignment
	static final int halfKeyWidth = keyWidth/2;
	static final int keyYRelative = (heightPerPlugin-keyHeight)/2;
	static final int halfKeyHeight = keyHeight/2;
	static final int halfPluginHeight = heightPerPlugin/2;
	
	static final Color keyOutlineColor = Color.BLACK;
	static final Color keyOutlineSelectedColor = Color.BLACK;
	
	private Timeline timeline;
	
	public TimelinePanel(Timeline timeline) {
		setLayout(null); // required for absolute positioning
		setBackground(Color.WHITE);
		this.timeline = timeline;
		timeline.addAnimationTimeListener(this);
		
//		JLabel testLabel = new JLabel(new Icon)
		
		this.addMouseListener(this);
		
		rebuildKeyLabels();
	}
	
	private synchronized void rebuildKeyLabels() {
		removeAll();
		int pluginY = headerHeight;
		for (int index=0; index < timeline.getNumPlugins(); index++) {
			KeyFrameList keys = timeline.getKeysForPlugin(index);
			for (KeyFrame key : keys) {
				KeyFrameLabel label = buildKeyLabel(key, keys);
				
				// this is the left side of of the icon so subtract half width
				int x = getPixelX(key.getStartTime()) - halfKeyWidth;
				// this is the top of the icon
				int y = pluginY + keyYRelative;
				add(label);
				label.setLocation(x, y);
				label.updateSize();
			}
			pluginY += heightPerPlugin;
		}
	}
	
	private KeyFrameLabel buildKeyLabel(KeyFrame key, KeyFrameList keys) {
		KeyFrameLabel label = new KeyFrameLabel(this, key, keys);
		label.addMouseListener(this);
		label.addMouseMotionListener(this);
		return label;
	}
	
	@Override
	protected synchronized void paintComponent(Graphics g) {
		super.paintComponent(g);
		int panelHeight = getHeight();
		int panelWidth = getWidth();
		
		double curMaxTime = getCurrentTotalTime();
		
		// draw header
		drawTicks(g, minorTickInterval, minorTickHeight, panelWidth, panelHeight, curMaxTime, false);
		drawTicks(g, majorTickInterval, majorTickHeight, panelWidth, panelHeight, curMaxTime, true);
		
		int y = headerHeight;
		
		g.setColor(pluginDividerColor);
		g.drawLine(0, y, panelWidth, y);
		
		for (int index=0; index < timeline.getNumPlugins(); index++) {
			
			y += heightPerPlugin;
			g.setColor(pluginDividerColor);
			g.drawLine(0, y, panelWidth, y);
		}
		
		// draw line at current time
		g.setColor(lineColor);
		int curTimePixel = getPixelX(curTime);
//		System.out.println("Paint with time: "+curTime+", pixel="+curTimePixel);
		g.drawLine(curTimePixel, 0, curTimePixel, panelHeight);
	}
	
	static Color fillColorForKey(KeyFrame key) {
		if (key instanceof VisibilityKeyFrame) {
			VisibilityKeyFrame v = (VisibilityKeyFrame)key;
			if (v.isVisible())
				return Color.BLUE;
			else
				return Color.GRAY;
		}
		if (key instanceof RangeKeyFrame)
			return Color.GREEN;
		return Color.RED;
	}
	
	private void drawTicks(Graphics g, double interval, int tickHeight, int panelWidth, int panelHeight,
			double maxTime, boolean label) {
		if (label)
			g.setFont(timeLabelFont);
		g.setColor(tickColor);
		for (double time=0; time<=maxTime; time+=interval) {
			int x = getPixelX(time);
			if (tickGridColor != null) {
				// draw grid
				g.setColor(tickGridColor);
				g.drawLine(x, 0, x, panelHeight);
				g.setColor(tickColor);
			}
			g.drawLine(x, 0, x, tickHeight);
			if (label)
				g.drawString(timeLabelFormat.format(time), x, headerHeight-timeLabelLowerPad);
		}
	}
	
	int getPixelX(double time) {
		return (int)(time * pixelsPerSecond + 0.5);
	}
	
	double getTime(int x) {
		double time = x * secondsPerPixel;
		
		// round to tick interval
		return Math.round(time/minorTickInterval)*minorTickInterval;
	}
	
	private int pluginIndexForY(int y) {
		if (y < headerHeight)
			return -1;
		return (y - headerHeight)/heightPerPlugin;
	}
	
	public double getCurrentTotalTime() {
		return getWidth()*secondsPerPixel;
	}
	
	private double getTime(MouseEvent e) {
		int x;
		if (e.getSource() == this)
			// already timeline coordinates
			x = e.getX();
		else
			x = e.getX() + e.getComponent().getX();
		return getTime(x);
	}
	
	private void removeKey(KeyFrameLabel label) {
		label.getKeyFrameList().removeKeyFrame(label.getKeyFrame());
		remove(label);
	}
	
	private void addKey(double time, int pluginIndex, boolean forceVisibilityKey) {
		Plugin plugin = timeline.getPluginAt(pluginIndex);
		KeyFrame key;
		if (!forceVisibilityKey && plugin instanceof StatefulPlugin) {
			PluginState state = ((StatefulPlugin)plugin).getState().deepCopy();
			if (plugin instanceof RangeAnimationListener) {
				// can add a range or regular key
				String durStr = JOptionPane.showInputDialog(
						this, "Duration (min: "+minorTickInterval+", 0 for standard KeyFrame)", "1");
				if (durStr == null)
					return;
				try {
					double duration = Double.parseDouble(durStr);
					if (duration == 0) {
						key = new KeyFrame(time, state);
					} else {
						Preconditions.checkState(duration > minorTickInterval);
						key = new RangeKeyFrame(time, time+duration, state);
					}
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(this, "Could not parse duration: "+durStr,
							"Error Parsing Duration", JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else {
				key = new KeyFrame(time, state);
			}
		} else {
			JCheckBox check = new JCheckBox("Visible?");
			check.setSelected(true);
			String title;
			if (plugin instanceof StatefulPlugin)
				title = "New Visibility KeyFrame";
			else
				title = "Plugin Only Supports Visibility Animation";
			int ret = JOptionPane.showConfirmDialog(
					this, check, title, JOptionPane.OK_CANCEL_OPTION);
			if (ret == JOptionPane.OK_OPTION)
				key = new VisibilityKeyFrame(time, timeline.getActorsForPlugin(pluginIndex), check.isSelected());
			else
				return;
		}
		
		timeline.addKeyFrame(pluginIndex, key);
		rebuildKeyLabels();
	}

	@Override
	public synchronized void mouseClicked(MouseEvent e) {
//		double time = getTime(e);
//		System.out.println("click x="+e.getX()+", y="+e.getY()+", class="
//				+ClassUtils.getClassNameWithoutPackage(e.getSource().getClass())+", time="+time);
		
		if (e.getSource() instanceof KeyFrameLabel) {
			KeyFrameLabel label = (KeyFrameLabel)e.getSource();
			KeyFrame key = label.getKeyFrame();
			
			boolean remove = false;
			if (e.getClickCount() == 2 && e.getSource() instanceof KeyFrameLabel && !(key instanceof RangeKeyFrame)) {
				int ret = JOptionPane.showConfirmDialog(
						this, "Delete Key Frame?", "Delete?", JOptionPane.OK_CANCEL_OPTION);
				remove = ret == JOptionPane.OK_OPTION;
			} else if (e.getButton() == MouseEvent.BUTTON2) {
				remove = true;
			}
			
			if (remove) {
				removeKey(label);
			} else if (e.getClickCount() == 2 && label.getKeyFrame() instanceof RangeKeyFrame) {
				// update duration
				RangeKeyFrame range = (RangeKeyFrame)label.getKeyFrame();
				String durStr = JOptionPane.showInputDialog(
						this, "Update Duration (min: "+minorTickInterval+", 0 to delete)", ""+range.getDuration());
				if (durStr == null)
					return;
				try {
					double duration = Double.parseDouble(durStr);
					if (duration == 0) {
						removeKey(label);
					} else {
						Preconditions.checkState(duration > minorTickInterval);
						range.setDuration(duration);
						label.updateSize();
						label.repaint();
					}
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(this, "Could not parse duration: "+durStr,
							"Error Setting Duration", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if (e.getSource() == this && e.getClickCount() == 2) {
			// double clicked empty space
			
			int pluginIndex = pluginIndexForY(e.getY());
			double time = getTime(e.getX());
			if (pluginIndex < 0) {
				// clicked header, go to that time
				System.out.println("Going to time "+time);
				timeline.activateTime(time);
			} else if (pluginIndex < timeline.getNumPlugins()) {
				// add keyframe
				System.out.println("Adding KeyFrame at time "+time+", plugin "+pluginIndex);
				addKey(time, pluginIndex, e.isShiftDown());
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
//		double time = getTime(e);
//		System.out.println("press x="+e.getX()+", y="+e.getY()+", class="
//				+ClassUtils.getClassNameWithoutPackage(e.getSource().getClass())+", time="+time);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
//		double time = getTime(e);
//		System.out.println("release x="+e.getX()+", y="+e.getY()+", class="
//				+ClassUtils.getClassNameWithoutPackage(e.getSource().getClass())+", time="+time);
		if (e.getSource() instanceof KeyFrameLabel) {
			KeyFrameLabel label = (KeyFrameLabel)e.getSource();
			if (label.isDragging())
				label.finalizeDrag();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		Preconditions.checkState(e.getSource() instanceof KeyFrameLabel);
		double time = getTime(e);
//		System.out.println("drag x="+e.getX()+", y="+e.getY()+", class="
//				+ClassUtils.getClassNameWithoutPackage(e.getSource().getClass())+", time="+time);
		KeyFrameLabel label = (KeyFrameLabel)e.getSource();
		if (e.isShiftDown() && !label.isDragging()) {
			// duplicate in place and drag the old one
			KeyFrame dupKey = label.getKeyFrame().duplicate();
			label.getKeyFrameList().addKeyFrame(dupKey);
			KeyFrameLabel dupLabel = buildKeyLabel(dupKey, label.getKeyFrameList());
			add(dupLabel);
			dupLabel.setLocation(label.getX(), label.getY());
			dupLabel.updateSize();
		}
		label.dragTo(time);
	}

	@Override
	public void mouseMoved(MouseEvent e) {}

	@Override
	public void animationTimeChanged(double time) {
		this.curTime = time;
		this.repaint();
	}

}
