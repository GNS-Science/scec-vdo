package org.scec.vtk.plugins.opensha;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.scec.geo3d.library.wgcep.faults.colorers.AseismicityColorer;
import org.scec.geo3d.library.wgcep.faults.colorers.CouplingCoefficientColorer;
import org.scec.geo3d.library.wgcep.faults.colorers.FaultColorer;
import org.scec.geo3d.library.wgcep.surfaces.GeometryGenerator;
import org.scec.vtk.plugins.opensha.builders.FaultModelBuilder;

import vtk.vtkActor;

public class FaultModelPlugin extends AbstractFaultPlugin {

	@Override
	public ArrayList<vtkActor> getActors() {
		throw new UnsupportedOperationException("I doubt this is used, this execption will help me find out");
	}

	@Override
	protected FaultPluginGUI buildGUI() throws Exception {
//		ConnectionPointsDisplayPanel connsPanel = new ConnectionPointsDisplayPanel();
		FaultModelBuilder builder = new FaultModelBuilder();
		ArrayList<FaultColorer> colorers = FaultPluginGUI.createDefaultColorers();
		colorers.add(new AseismicityColorer());
		colorers.add(new CouplingCoefficientColorer());
//		colorers.add(new OnlyNewColorer(false)); // TODO
		ArrayList<GeometryGenerator> geomGens = FaultPluginGUI.createDefaultGeomGens();
		
		FaultPluginGUI gui = new FaultPluginGUI(builder, colorers, geomGens, Color.GRAY);
		// TODO
//		gui.addDistTab();
//		gui.addTab("Section Connections", connsPanel);
//		FaultZonesPanel zonesPanel = new FaultZonesPanel(gui.getEventManager());
//		gui.addTab("Zone Polygons", zonesPanel);
		return gui;
	}

}
