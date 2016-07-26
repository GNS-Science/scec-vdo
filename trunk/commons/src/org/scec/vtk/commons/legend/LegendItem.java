package org.scec.vtk.commons.legend;

import org.scec.vtk.plugins.Plugin;

import com.google.common.base.Preconditions;

import vtk.vtkActor2D;

public class LegendItem {
	
	private vtkActor2D actor;
	private Plugin source;
	private String title;
	
	public LegendItem(vtkActor2D actor, Plugin source, String title) {
		super();
		Preconditions.checkNotNull(actor, "Legend actor cannot be null");
		Preconditions.checkNotNull(source, "Legend source cannot be null");
		this.actor = actor;
		this.source = source;
		this.title = title;
	}

	public vtkActor2D getActor() {
		return actor;
	}

	public Plugin getSource() {
		return source;
	}

	public String getTitle() {
		return title;
	}
	
	public String toString() {
		return source.getMetadata().getShortName()+": "+title;
	}

}
