package org.goko.tinyg.controller;

import java.math.BigDecimal;

import org.goko.controller.tinyg.controller.TinyGCommunicator;
import org.goko.controller.tinyg.controller.TinyGControllerService;
import org.goko.controller.tinyg.controller.configuration.TinyGConfiguration;
import org.goko.controller.tinyg.controller.configuration.TinyGConfigurationValue;
import org.goko.core.common.exception.GkFunctionalException;
import org.goko.core.common.measure.quantity.Length;
import org.goko.core.common.measure.quantity.LengthUnit;
import org.goko.core.common.measure.quantity.Speed;
import org.goko.core.common.measure.quantity.SpeedUnit;
import org.goko.core.connection.serial.SerialParameter;
import org.goko.core.controller.bean.EnumControllerAxis;
import org.goko.core.controller.bean.MachineState;
import org.goko.core.gcode.rs274ngcv3.IRS274NGCService;
import org.goko.core.gcode.rs274ngcv3.RS274NGCServiceImpl;
import org.goko.core.gcode.rs274ngcv3.context.CoordinateSystem;
import org.goko.core.gcode.rs274ngcv3.context.EnumDistanceMode;
import org.goko.core.gcode.rs274ngcv3.context.EnumMotionMode;
import org.goko.core.gcode.rs274ngcv3.context.EnumPlane;
import org.goko.core.gcode.rs274ngcv3.context.GCodeContext;
import org.goko.core.math.Tuple6b;
import org.goko.junit.tools.assertion.AssertGkFunctionalException;
import org.goko.junit.tools.connection.AssertSerialEmulator;
import org.goko.junit.tools.connection.SerialConnectionEmulator;

import junit.framework.TestCase;

/*
 
{"r":{"fv":0.970,"fb":435.10,"hp":1,"hv":8,"id":"9H3583-PXN","msg":"SYSTEM READY"},"f":[1,0,0,3412]}
{"qr":28,"qi":1,"qo":1}
{"r":{"sr":{"line":0,"posx":0.000,"posy":0.000,"posz":0.000,"posa":0.000,"feed":0.00,"vel":0.00,"unit":1,"coor":1,"dist":0,"frmo":1,"momo":4,"stat":1}},"f":[1,0,10,8936]}
{"sr":{"line":0,"posx":0.000,"posy":0.000,"posz":0.000,"posa":0.000,"feed":0.00,"vel":0.00,"unit":1,"coor":1,"dist":0,"frmo":1,"momo":4,"stat":1}}
{"r":{"qr":28},"f":[1,0,10,8758]}
{"r":{"1":{"ma":0,"sa":0.900,"tr":32.0000,"mi":8,"po":1,"pm":1}},"f":[1,0,9,8694]}
{"r":{"2":{"ma":1,"sa":0.900,"tr":32.0000,"mi":8,"po":1,"pm":1}},"f":[1,0,9,5984]}
{"r":{"3":{"ma":2,"sa":1.800,"tr":2.1170,"mi":8,"po":0,"pm":1}},"f":[1,0,9,3553]}
{"r":{"4":{"ma":3,"sa":1.800,"tr":75.0000,"mi":8,"po":1,"pm":1}},"f":[1,0,9,8385]}
{"r":{"x":{"am":1,"vm":1500,"fr":1500,"tn":0.000,"tm":300.000,"jm":5000,"jh":30000,"jd":0.0100,"sn":0,"sx":1,"sv":1000,"lv":100,"lb":20.000,"zb":3.000}},"f":[1,0,9,1167]}
{"r":{"y":{"am":1,"vm":1500,"fr":1500,"tn":0.000,"tm":220.000,"jm":5000,"jh":30000,"jd":0.0100,"sn":0,"sx":1,"sv":1000,"lv":100,"lb":20.000,"zb":3.000}},"f":[1,0,9,716]}
{"r":{"z":{"am":1,"vm":1000,"fr":1000,"tn":0.000,"tm":100.000,"jm":50,"jh":1000,"jd":0.0100,"sn":1,"sx":0,"sv":800,"lv":100,"lb":20.000,"zb":10.000}},"f":[1,0,9,6257]}
{"r":{"a":{"am":1,"vm":48000,"fr":48000,"tn":-1.000,"tm":-1.000,"jm":24000,"jh":24000,"jd":0.1000,"ra":0.000,"sn":0,"sx":0,"sv":6000,"lv":1000,"lb":5.000,"zb":2.000}},"f":[1,0,9,7419]}
{"r":{"sys":{"fb":435.10,"fv":0.970,"hp":1,"hv":8,"id":"9H3583-PXN","ja":2000000,"ct":0.0100,"sl":0,"st":1,"mt":180.00,"ej":1,"jv":5,"js":1,"tv":1,"qv":2,"sv":1,"si":100,"ec":0,"ee":0,"ex":2,"baud":5,"net":0,"gpl":0,"gun":1,"gco":1,"gpa":0,"gdi":0}},"f":[1,0,11,346]}
{"r":{"g55":{"x":-164.127,"y":-208.999,"z":53.778,"a":0.000,"b":0.000,"c":0.000}},"f":[1,0,11,1231]}
{"r":{"g56":{"x":-84.739,"y":-103.984,"z":-10.000,"a":0.000,"b":0.000,"c":0.000}},"f":[1,0,11,3338]}
{"r":{"g57":{"x":0.000,"y":0.000,"z":0.000,"a":0.000,"b":0.000,"c":0.000}},"f":[1,0,11,8386]}
{"r":{"g58":{"x":-8.931,"y":9.450,"z":0.000,"a":0.000,"b":0.000,"c":0.000}},"f":[1,0,11,2282]}
{"r":{"g59":{"x":0.434,"y":17.316,"z":0.000,"a":0.000,"b":0.000,"c":0.000}},"f":[1,0,11,4046]}
 
 */
public class TinyGControllerServiceTestCase extends TestCase {
	private TinyGControllerService 	 tinyg;
	private SerialConnectionEmulator serialEmulator;
	private IRS274NGCService gcodeService;
	
	/** {@inheritDoc}
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {		
		super.setUp();
		serialEmulator = new SerialConnectionEmulator();
		serialEmulator.setDebugOutputConsole(true);
		TinyGCommunicator communicator = new TinyGCommunicator();
		communicator.setConnectionService(serialEmulator);
		tinyg = new TinyGControllerService(communicator);				
		gcodeService = new RS274NGCServiceImpl();
		tinyg.setGCodeService(gcodeService);
		tinyg.start(); 				  // Start the TinyG service
		serialEmulator.connect(null, null, null, null, null, null); // Make sure the service is connected
		tinyg.setPlannerBufferCheck(false);		
	}
	
	/**
	 * Simple test for configuration update
	 * @throws Exception Exception
	 */
	public void testConfigurationUpdate() throws Exception{	
		BigDecimal queueReportVerbosity = tinyg.getConfiguration().getSetting(TinyGConfiguration.QUEUE_REPORT_VERBOSITY, BigDecimal.class);
		assertEquals(BigDecimal.ZERO, queueReportVerbosity);
		
		serialEmulator.receiveData("{\"r\":{\"qv\":2},\"f\":[1,0,10,4252]}"+'\n');
		BigDecimal queueReportVerbosityUpdated = tinyg.getConfiguration().getSetting(TinyGConfiguration.QUEUE_REPORT_VERBOSITY, BigDecimal.class);
		assertEquals(new BigDecimal("2"), queueReportVerbosityUpdated);
	}
	
	/**
	 * Context : TinyG has no motion control enabled and we try to run a motion
	 * Result  : TinyG throws an exception. Motion control is required 
	 * @throws Exception
	 */
	public void testNoMotionControlEnabled() throws Exception{
		// Emulate the reception of the GCode context
		serialEmulator.receiveDataWithEndChar("{\"r\":{\"sr\":{\"line\":0,\"posx\":0.000,\"posy\":0.000,\"posz\":0.000,\"posa\":0.000,\"feed\":0.00,\"vel\":0.00,\"unit\":1,\"coor\":1,\"dist\":0,\"frmo\":1,\"momo\":4,\"stat\":1}},\"f\":[1,0,0,0]}");
		serialEmulator.receiveDataWithEndChar("{\"r\":{\"ex\":2, \"jv\":5},\"f\":[1,0,0,0]}");
		serialEmulator.getCurrentConnection().setFlowControl(SerialParameter.FLOWCONTROL_RTSCTS);
		tinyg.setPlannerBufferCheck(true);
		// Let's check that flow control is enabled
		assertEquals(new BigDecimal("2"), tinyg.getConfiguration().getSetting(TinyGConfiguration.ENABLE_FLOW_CONTROL, BigDecimal.class));
		
		try{
			tinyg.verifyReadyForExecution();
			fail();
		}catch(GkFunctionalException e){
			AssertGkFunctionalException.assertException(e, "TNG-002");
		}
	}
	
	/**
	 * Context : TinyG has no flow control enabled and we try to run a motion
	 * Result  : TinyG throws an exception. Flow control is required 
	 * @throws Exception
	 */
	public void testNoFlowControlEnabled() throws Exception{
		// Emulate the reception of the GCode context
		serialEmulator.receiveDataWithEndChar("{\"r\":{\"sr\":{\"line\":0,\"posx\":0.000,\"posy\":0.000,\"posz\":0.000,\"posa\":0.000,\"feed\":0.00,\"vel\":0.00,\"unit\":1,\"coor\":1,\"dist\":0,\"frmo\":1,\"momo\":4,\"stat\":1}},\"f\":[1,0,0,0]}");
		serialEmulator.receiveDataWithEndChar("{\"r\":{\"ex\":0},\"f\":[1,0,0,0]}");
		
		tinyg.setPlannerBufferCheck(true);
		// Let's check that flow control is enabled
		assertEquals(new BigDecimal("0"), tinyg.getConfiguration().getSetting(TinyGConfiguration.ENABLE_FLOW_CONTROL, BigDecimal.class));
				
		try{
			tinyg.verifyReadyForExecution();
			fail();
		}catch(GkFunctionalException e){
			AssertGkFunctionalException.assertException(e, "TNG-001");
		}
	}
	
	/**
	 * Context : TinyG has a flow control enabled but it's not the same as the currently active serial connection
	 * Result  : TinyG throws an exception. Flow control on TinyG and Serial connection have to match
	 * @throws Exception
	 */
	public void testNonMatchingFlowControlRtsCts() throws Exception{
		// Emulate the reception of the GCode context
		serialEmulator.receiveDataWithEndChar("{\"r\":{\"sr\":{\"line\":0,\"posx\":0.000,\"posy\":0.000,\"posz\":0.000,\"posa\":0.000,\"feed\":0.00,\"vel\":0.00,\"unit\":1,\"coor\":1,\"dist\":0,\"frmo\":1,\"momo\":4,\"stat\":1}},\"f\":[1,0,0,0]}");
		serialEmulator.receiveDataWithEndChar("{\"r\":{\"ex\":2},\"f\":[1,0,0,0]}");
		
		tinyg.setPlannerBufferCheck(true);
		// Let's check that flow control is enabled
		assertEquals(TinyGConfigurationValue.FLOW_CONTROL_RTS_CTS, tinyg.getConfiguration().getSetting(TinyGConfiguration.ENABLE_FLOW_CONTROL, BigDecimal.class));
				
		try{
			tinyg.verifyReadyForExecution();
			fail();
		}catch(GkFunctionalException e){
			AssertGkFunctionalException.assertException(e, "TNG-005");
		}
	}
	
	/**
	 * Context : TinyG has a flow control enabled but it's not the same as the currently active serial connection
	 * Result  : TinyG throws an exception. Flow control on TinyG and Serial connection have to match
	 * @throws Exception
	 */
	public void testNonMatchingFlowControlXonXoff() throws Exception{
		// Emulate the reception of the GCode context
		serialEmulator.receiveDataWithEndChar("{\"r\":{\"sr\":{\"line\":0,\"posx\":0.000,\"posy\":0.000,\"posz\":0.000,\"posa\":0.000,\"feed\":0.00,\"vel\":0.00,\"unit\":1,\"coor\":1,\"dist\":0,\"frmo\":1,\"momo\":4,\"stat\":1}},\"f\":[1,0,0,0]}");
		serialEmulator.receiveDataWithEndChar("{\"r\":{\"ex\":1},\"f\":[1,0,0,0]}");
		
		tinyg.setPlannerBufferCheck(true);
		// Let's check that flow control is enabled
		assertEquals(TinyGConfigurationValue.FLOW_CONTROL_XON_XOFF, tinyg.getConfiguration().getSetting(TinyGConfiguration.ENABLE_FLOW_CONTROL, BigDecimal.class));
				
		try{
			tinyg.verifyReadyForExecution();
			fail();
		}catch(GkFunctionalException e){
			AssertGkFunctionalException.assertException(e, "TNG-006");
		}
	}
	
	/**
	 * Context : We receive notification about the modification of the units in the GCode context
	 * Result  : TinyG controller updates it's internal units
	 * @throws Exception
	 */
	public void testGCodeContextUnitChange() throws Exception{			
		serialEmulator.clearOutputBuffer();
		serialEmulator.clearSentBuffer();
		
		serialEmulator.receiveDataWithEndChar("{\"r\":{\"sr\":{\"unit\":0}},\"f\":[1,0,0,0]}");		
		assertEquals(LengthUnit.INCH, tinyg.getCurrentUnit());
		
		serialEmulator.receiveDataWithEndChar("{\"r\":{\"sr\":{\"unit\":1}},\"f\":[1,0,0,0]}");		
		assertEquals(LengthUnit.MILLIMETRE, tinyg.getCurrentUnit());
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void testResetCurrentCoordinateSystem() throws Exception{		
		serialEmulator.clearOutputBuffer();
		serialEmulator.clearSentBuffer();			

		serialEmulator.receiveDataWithEndChar("{\"r\":{\"sr\":{\"posx\":15.031,\"posy\":35.000,\"posz\":-16.031}},\"f\":[1,0,0,0]}");
		// Enable G55 
		serialEmulator.receiveDataWithEndChar("{\"r\":{\"sr\":{\"coor\":2}},\"f\":[1,0,0,0]}"); 
			
		assertEquals(CoordinateSystem.G55, tinyg.getCurrentCoordinateSystem());
		tinyg.resetCurrentCoordinateSystem();		
		
		AssertSerialEmulator.assertOutputMessagePresent(serialEmulator, "{\"G55\":{\"X\":15.031,\"Y\":35,\"Z\":-16.031}}"+'\n', 1000);
	}
	
	/**
	 * Context : We receive notification about the coordinate system update offset
	 * Result  : TinyG controller updates it's internal units
	 * @throws Exception
	 */
	public void testGCodeContextCoordinateSystemOffsetChange() throws Exception{			
		serialEmulator.clearOutputBuffer();
		serialEmulator.clearSentBuffer();
		
		serialEmulator.receiveDataWithEndChar("{\"r\":{\"g55\":{\"x\":-10,\"y\":20,\"z\":35.53,\"a\":0,\"b\":0,\"c\":0}},\"f\":[1,0,12]}");		
		Tuple6b expectedG55 = new Tuple6b(Length.valueOf("-10", tinyg.getCurrentUnit()),
											Length.valueOf("20", tinyg.getCurrentUnit()),
											Length.valueOf("35.53", tinyg.getCurrentUnit()));
		Tuple6b actualG55 = tinyg.getGCodeContext().getCoordinateSystemData(CoordinateSystem.G55);
		assertEquals(expectedG55, actualG55);
		
	}
	
	/**
	 * Tests the various stat values in status report 
	 * @throws Exception Exception
	 */
	public void testStatusReportState() throws Exception{
		serialEmulator.clearOutputBuffer();
		serialEmulator.clearSentBuffer();
		{	// Stat 0 
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"stat\":0}}");
			assertEquals(tinyg.getState(), MachineState.INITIALIZING);
		}
		{	// Stat 1 
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"stat\":1}}");
			assertEquals(tinyg.getState(), MachineState.READY);
		}
		{	// Stat 2
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"stat\":2}}");
			assertEquals(tinyg.getState(), MachineState.ALARM);
		}
		{	// Stat 3 
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"stat\":3}}");
			assertEquals(tinyg.getState(), MachineState.PROGRAM_STOP);
		}
		{	// Stat 4 
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"stat\":4}}");
			assertEquals(tinyg.getState(), MachineState.PROGRAM_END);
		}
		{	// Stat 5 
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"stat\":5}}");
			assertEquals(tinyg.getState(), MachineState.MOTION_RUNNING);
		}
		{	// Stat 6 
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"stat\":6}}");
			assertEquals(tinyg.getState(), MachineState.MOTION_HOLDING);
		}
		{	// Stat 7 
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"stat\":7}}");
			assertEquals(tinyg.getState(), MachineState.PROBE_CYCLE);
		}		
		{	// Stat 9 
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"stat\":9}}");
			assertEquals(tinyg.getState(), MachineState.HOMING);
		}	
		
	}
		
	/**
	 * Tests the various stat values in status report 
	 * @throws Exception Exception
	 */
	public void testStatusReportVelocity() throws Exception{
		serialEmulator.clearOutputBuffer();
		serialEmulator.clearSentBuffer();
		serialEmulator.receiveDataWithEndChar("{\"sr\":{\"vel\":109.35}}");
		assertEquals(tinyg.getVelocity(), Speed.valueOf("109.35", SpeedUnit.MILLIMETRE_PER_MINUTE));
	}
	
	/**
	 * Tests the various feed values in status report 
	 * @throws Exception Exception
	 */
	public void testStatusReportDistanceMode() throws Exception{
		serialEmulator.clearOutputBuffer();
		serialEmulator.clearSentBuffer();
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"dist\":0}}");
			assertEquals(EnumDistanceMode.ABSOLUTE, tinyg.getGCodeContext().getDistanceMode());
		}
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"dist\":1}}");
			assertEquals(EnumDistanceMode.RELATIVE, tinyg.getGCodeContext().getDistanceMode());
		}
		
	}
	
	/**
	 * Tests the various feed values in status report 
	 * @throws Exception Exception
	 */
	public void testStatusReportCoordinateSystem() throws Exception{
		serialEmulator.clearOutputBuffer();
		serialEmulator.clearSentBuffer();
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"coor\":0}}");
			assertEquals(CoordinateSystem.G53, tinyg.getCurrentCoordinateSystem());
		}
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"coor\":1}}");
			assertEquals(CoordinateSystem.G54, tinyg.getCurrentCoordinateSystem());
		}
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"coor\":2}}");
			assertEquals(CoordinateSystem.G55, tinyg.getCurrentCoordinateSystem());
		}
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"coor\":3}}");
			assertEquals(CoordinateSystem.G56, tinyg.getCurrentCoordinateSystem());
		}
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"coor\":4}}");
			assertEquals(CoordinateSystem.G57, tinyg.getCurrentCoordinateSystem());
		}
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"coor\":5}}");
			assertEquals(CoordinateSystem.G58, tinyg.getCurrentCoordinateSystem());
		}
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"coor\":6}}");
			assertEquals(CoordinateSystem.G59, tinyg.getCurrentCoordinateSystem());
		}
	}
	
	/**
	 * Tests the various plane values in status report 
	 * @throws Exception Exception
	 */
	public void testStatusReportPlane() throws Exception{
		serialEmulator.clearOutputBuffer();
		serialEmulator.clearSentBuffer();
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"plan\":0}}");
			assertEquals(EnumPlane.XY_PLANE, tinyg.getGCodeContext().getPlane());
		}
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"plan\":1}}");
			assertEquals(EnumPlane.XZ_PLANE, tinyg.getGCodeContext().getPlane());
		}
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"plan\":2}}");
			assertEquals(EnumPlane.YZ_PLANE, tinyg.getGCodeContext().getPlane());
		}
	}
	
	/**
	 * Tests the various motion mode values in status report 
	 * @throws Exception Exception
	 */
	public void testStatusReportMotionMode() throws Exception{
		serialEmulator.clearOutputBuffer();
		serialEmulator.clearSentBuffer();
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"momo\":0}}");
			assertEquals(EnumMotionMode.RAPID, tinyg.getGCodeContext().getMotionMode());			
		}
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"momo\":1}}");
			assertEquals(EnumMotionMode.FEEDRATE, tinyg.getGCodeContext().getMotionMode());			
		}
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"momo\":2}}");
			assertEquals(EnumMotionMode.ARC_CLOCKWISE, tinyg.getGCodeContext().getMotionMode());			
		}
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"momo\":3}}");
			assertEquals(EnumMotionMode.ARC_COUNTERCLOCKWISE, tinyg.getGCodeContext().getMotionMode());			
		}
	}
	
	/**
	 * Tests the machine position values in status report 
	 * @throws Exception Exception
	 */
	public void testStatusReportMachinePosition() throws Exception{
		serialEmulator.clearOutputBuffer();
		serialEmulator.clearSentBuffer();
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"mpox\":-10,\"mpoy\":20,\"mpoz\":35.53}}");
			Tuple6b expectedPosition = new Tuple6b(Length.valueOf("-10", tinyg.getCurrentUnit()),
													Length.valueOf("20", tinyg.getCurrentUnit()),
													Length.valueOf("35.53", tinyg.getCurrentUnit()));
			assertEquals(expectedPosition, tinyg.getGCodeContext().getMachinePosition());		
		}
	}
	
	/**
	 * Tests the work position values in status report 
	 * @throws Exception Exception
	 */
	public void testStatusReportWorkPosition() throws Exception{
		serialEmulator.clearOutputBuffer();
		serialEmulator.clearSentBuffer();
		{
			serialEmulator.receiveDataWithEndChar("{\"sr\":{\"posx\":-10,\"posy\":20,\"posz\":35.53}}");
			Tuple6b expectedPosition = new Tuple6b(Length.valueOf("-10", tinyg.getCurrentUnit()),
													Length.valueOf("20", tinyg.getCurrentUnit()),
													Length.valueOf("35.53", tinyg.getCurrentUnit()));
			assertEquals(expectedPosition, tinyg.getGCodeContext().getPosition());		
		}
	}
	
	/* ************************************************
	 *  Jogging tests
	 * ************************************************/
	
	
	public void testJogStartRelative() throws Exception{
		serialEmulator.clearOutputBuffer();
		serialEmulator.clearSentBuffer();			

		serialEmulator.receiveDataWithEndChar("{\"r\":{\"sr\":{\"posx\":15.031,\"posy\":35.000,\"posz\":-16.031, \"dist\":1, \"stat\":1}},\"f\":[1,0,0,0]}");
		serialEmulator.receiveDataWithEndChar("{\"qr\":32,\"qi\":1,\"qo\":1}");
				
		GCodeContext context = tinyg.getGCodeContext();
		assertEquals(EnumDistanceMode.RELATIVE, context.getDistanceMode());

		
		tinyg.jog(EnumControllerAxis.X_POSITIVE, Length.valueOf(BigDecimal.ONE, LengthUnit.MILLIMETRE), Speed.valueOf("1000", SpeedUnit.MILLIMETRE_PER_MINUTE));		
		AssertSerialEmulator.assertOutputMessagePresent(serialEmulator, "G91G1F1000X1.000\n",1000);
		
		
		tinyg.jog(EnumControllerAxis.X_NEGATIVE, Length.valueOf(BigDecimal.ONE, LengthUnit.MILLIMETRE), Speed.valueOf("1000", SpeedUnit.MILLIMETRE_PER_MINUTE));		
		AssertSerialEmulator.assertOutputMessagePresent(serialEmulator, "G91G1F1000X-1.000\n",1000);
		
		
		tinyg.jog(EnumControllerAxis.Y_POSITIVE, Length.valueOf("0.01", LengthUnit.MILLIMETRE), Speed.valueOf("1000", SpeedUnit.MILLIMETRE_PER_MINUTE));		
		AssertSerialEmulator.assertOutputMessagePresent(serialEmulator, "G91G1F1000Y0.010\n",1000);
		
		
		tinyg.jog(EnumControllerAxis.Y_NEGATIVE, Length.valueOf("0.01", LengthUnit.MILLIMETRE), Speed.valueOf("1000", SpeedUnit.MILLIMETRE_PER_MINUTE));		
		AssertSerialEmulator.assertOutputMessagePresent(serialEmulator, "G91G1F1000Y-0.010\n",1000);
		
		
		tinyg.jog(EnumControllerAxis.Z_POSITIVE, Length.valueOf("2.035", LengthUnit.MILLIMETRE), Speed.valueOf("1000", SpeedUnit.MILLIMETRE_PER_MINUTE));		
		AssertSerialEmulator.assertOutputMessagePresent(serialEmulator, "G91G1F1000Z2.035\n",1000);
		
		
		tinyg.jog(EnumControllerAxis.Z_NEGATIVE, Length.valueOf(BigDecimal.ONE, LengthUnit.MILLIMETRE), Speed.valueOf("1000", SpeedUnit.MILLIMETRE_PER_MINUTE));		
		AssertSerialEmulator.assertOutputMessagePresent(serialEmulator, "G91G1F1000Z-1.000\n",1000);
		
	}
	/** (inheritDoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		tinyg.stop();
	};
}
