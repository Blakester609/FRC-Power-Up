/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team4598.robot;

import java.util.Timer;
import java.util.TimerTask;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.hal.AllianceStationID;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.MotorSafety;
import edu.wpi.first.wpilibj.MotorSafetyHelper;
import edu.wpi.first.wpilibj.SPI;




/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends TimedRobot {
	NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
	NetworkTableEntry tx = table.getEntry("tx");
	NetworkTableEntry ty = table.getEntry("ty");
	NetworkTableEntry ta = table.getEntry("ta");
	NetworkTableEntry tv = table.getEntry("tv");
	NetworkTableEntry ledMode = table.getEntry("ledMode");
	NetworkTableEntry camMode = table.getEntry("camMode");
	NetworkTableEntry pipeline0 = table.getEntry("pipeline");
	int targetValue = (int) tv.getDouble(0);
	double horizontalOffset = tx.getDouble(0);
	double verticalOffset = ty.getDouble(0);
	double targetArea = ta.getDouble(0);
	
	private static int counter = 0;
	//Victor rightDrive1 = new Victor(0);
	Talon rightDrive1 = new Talon(0);
	Talon rightDrive2 = new Talon(2);
	public SpeedControllerGroup rightSideDrive = new SpeedControllerGroup(rightDrive1, rightDrive2);
	
	//Victor leftDrive1 = new Victor(1);
	Talon leftDrive1 = new Talon(1);
	Talon leftDrive2 = new Talon(3);
	public SpeedControllerGroup leftSideDrive = new SpeedControllerGroup(leftDrive1, leftDrive2);
	
	
	XboxController controller = new XboxController(1);
	
	Spark allThreadMotor = new Spark(4);
	Spark winchMotor = new Spark(7);
	
	Spark clawMotor1 = new Spark(5);
	Spark clawMotor2 = new Spark(6);
	
	DigitalInput limitAllThreadUp = new DigitalInput(1);
	DigitalInput limitAllThreadDown = new DigitalInput(2);
	boolean allThreadMotionUp;
	boolean allThreadMotionDown;
	
	ADXRS450_Gyro gyro  = new ADXRS450_Gyro(SPI.Port.kOnboardCS0);
	
	public int autoCounter;
	

	String gameData;
	
	String autoCommand;
	
	MotorSafetyHelper safetyRightDrive1 = new MotorSafetyHelper(rightDrive1);
	MotorSafetyHelper safetyRightDrive2 = new MotorSafetyHelper(rightDrive2);
	MotorSafetyHelper safetyLeftDrive1 = new MotorSafetyHelper(leftDrive1);
	MotorSafetyHelper safetyLeftDrive2 = new MotorSafetyHelper(leftDrive2);
	
	private static final String red1 = AllianceStationID.Red1.toString();
	private static final String red2 = AllianceStationID.Red2.toString();
	private static final String red3 = AllianceStationID.Red3.toString();
	private static final String blue1 = AllianceStationID.Blue1.toString();
	private static final String blue2 = AllianceStationID.Blue2.toString();
	private static final String blue3 = AllianceStationID.Blue3.toString();
	private String playerStation;
	private SendableChooser<String> playerStationChooser = new SendableChooser<>();
			
	String autoCommand1 = "autonomous 1";
	String autoCommand2 = "autonomous 2";
	String autoCommand3 = "autonomous 3";
	String autoCommand4 = "autonomous 4";
	String autoCommand5 = "autonomous 5";
	String autoCommand6 = "autonomous 6";
	
	
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		playerStationChooser.addDefault("Red 2", red2);
		playerStationChooser.addObject("Red 1", red1);
		playerStationChooser.addObject("Red 3", red3);
		playerStationChooser.addObject("Blue 1", blue1);
		playerStationChooser.addObject("Blue 2", blue2);
		playerStationChooser.addObject("Blue 3", blue3);
		SmartDashboard.putData("Player Stations", playerStationChooser);
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		
		rightDrive1.setSafetyEnabled(false);
		rightDrive2.setSafetyEnabled(false);
		leftDrive1.setSafetyEnabled(false);
		leftDrive2.setSafetyEnabled(false);
	}
	public void visionTrackingRight() {
		if(tv.getDouble(0) == 1.0) {
			leftSideDrive.set(0.4);
			rightSideDrive.set(-0.4); 
			}
		if(ta.getDouble(0) >= 20.0) {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		}
		if(tv.getDouble(0) != 1) {
			leftSideDrive.set(-0.3);
			rightSideDrive.set(-0.3);
		}
	}
	
	public void visionTrackingLeft() {
		if(tv.getDouble(0) == 1.0) {
			leftSideDrive.set(0.4);
			rightSideDrive.set(-0.4); 
			}
		if(ta.getDouble(0) >= 20.0) {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		}
		if(tv.getDouble(0) != 1) {
			leftSideDrive.set(0.3);
			rightSideDrive.set(0.3);
		}
		

	}
	
	private void autonomousOneWithLimelight() {
		autoCounter++;
		if(autoCounter < 50 && autoCounter < 51) {
			leftSideDrive.set(0.4);
			rightSideDrive.set(0);
		} 
		gyro.reset();
		if(autoCounter > 52 && autoCounter < 152) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.015));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.015));
		} else if(autoCounter > 153 && autoCounter < 172) {
			leftSideDrive.set(0);
			rightSideDrive.set(-0.4);
		}
		gyro.reset();
		if(autoCounter > 202 && autoCounter < 282) {
			visionTrackingLeft();
		}
	}
	
	private void autonomousTwoWithLimelight() {
		autoCounter++;
		if(autoCounter < 20 && autoCounter < 21) {
			leftSideDrive.set(0);
			rightSideDrive.set(-0.4);
		}
		gyro.reset();
		if(autoCounter > 22 && autoCounter < 72) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.015));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.015));
		} else if(autoCounter > 72 && autoCounter < 132) {
			leftSideDrive.set(0.4);
			rightSideDrive.set(0);
		}				
		gyro.reset();
		if(autoCounter > 132 && autoCounter < 892) {
			visionTrackingLeft();
		}
	}
	
	private void autonomousOneWithoutLimelight() {
		gyro.reset();
		autoCounter++;
		if(autoCounter < 60 && autoCounter < 61) {
			leftSideDrive.set(0.4);
			rightSideDrive.set(0);
		}
		gyro.reset();
		if(autoCounter > 61 && autoCounter < 120) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.015));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.015));
		} else if(autoCounter > 120 && autoCounter < 165) {
			leftSideDrive.set(0);
			rightSideDrive.set(-0.4);
		}				
		gyro.reset();
		if(autoCounter > 165 && autoCounter < 275) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.03));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.1));
		} else if(autoCounter > 277) {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		}
	}
	
	private void autonomousTwoWithoutLimelight() {
		gyro.reset();
		autoCounter++;
		if(autoCounter < 20 && autoCounter < 21) {
			leftSideDrive.set(0);
			rightSideDrive.set(-0.4);
		}
		gyro.reset();
		if(autoCounter > 21 && autoCounter < 70) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.015));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.015));
		} else if(autoCounter > 70 && autoCounter < 110) {
			leftSideDrive.set(0.4);
			rightSideDrive.set(0);
		}				
		gyro.reset();
		if(autoCounter > 110 && autoCounter < 162) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.03));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.1));
		} else if(autoCounter > 162) {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		}
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * <p>You can add additional auto modes by adding additional comparisons to
	 * the switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		autoCounter = 0;
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		playerStation = playerStationChooser.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		playerStation = blue2;
		System.out.println("Player Station: " + playerStation);
		if(playerStation == blue2) {
			if(gameData.length() > 0) {
				if(gameData.charAt(0) == 'L') {
						autoCommand = "autonomous 1";
				} else if(gameData.charAt(0) == 'R') {
					autoCommand = "autonomous 2";
				}
			}
		}
		
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		camMode.setNumber(0);
		pipeline0.setNumber(0);
		ledMode.setNumber(0);
		switch (autoCommand) {
		case "autonomous 1":
//			autonomousOneWithLimelight();
			autonomousOneWithoutLimelight();
			break;
		case "autonomous 2":
			//autonomousTwoWithLimelight();
			autonomousTwoWithoutLimelight();
			break;
			}
	}
		


	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		
		ledMode.setNumber(1);
		camMode.setNumber(1);
		// Drive
		double reduceLeftSpeed = 0;
		double reduceRightSpeed = 0;
		
		if(controller.getRawAxis(1) < -0.01) {
			reduceLeftSpeed -= controller.getRawAxis(0)*0.225;
			reduceRightSpeed -= controller.getRawAxis(0)*0.225;
		} else if(controller.getRawAxis(1) > 0.01) {
			reduceLeftSpeed += controller.getRawAxis(0)*0.225;
			reduceRightSpeed += controller.getRawAxis(0)*0.225;
		} else {
			reduceLeftSpeed = 0;
			reduceRightSpeed = 0;
		}
		
		if(controller.getRawAxis(1) != 0 && controller.getRawAxis(0) != 0) {
			double leftSpeed = 0.5*-controller.getRawAxis(1)+reduceLeftSpeed;
			double rightSpeed = 0.5*controller.getRawAxis(1)+reduceRightSpeed;	
			leftSideDrive.set(leftSpeed);
			rightSideDrive.set(rightSpeed);
		} else {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		}
		// Dime turn
		if(controller.getRawAxis(0) > 0.1 && controller.getRawAxis(0) < 0.1 && controller.getRawAxis(1) < 0.1 && controller.getRawAxis(1) > -0.1) {
			leftSideDrive.set(0.5*controller.getRawAxis(0));
			rightSideDrive.set(0.5*-controller.getRawAxis(0));
		} else {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		}
		
		
		
		
		// Winch Control
		if(controller.getAButton()) {
			winchMotor.set(0.4);
		} else if(controller.getBButton()) {
			winchMotor.set(-0.4);
		} else if(controller.getXButton()) {
			winchMotor.set(0.6);
		} else if(controller.getYButton()) {
			winchMotor.set(-0.6);
		} else {
			winchMotor.set(0);
		}
		
		if(limitAllThreadUp.get()) {
			allThreadMotionUp = false;
		} 
		if(!limitAllThreadUp.get()) {
			allThreadMotionUp = true;
		}
		if(limitAllThreadDown.get()) {
			allThreadMotionDown = false;
		} 
		if(!limitAllThreadDown.get()) {
			allThreadMotionDown = true;
		}
		
		
		
		// All Thread Control
		if((controller.getAButton() && controller.getRawAxis(3) == 1.0) && allThreadMotionUp == true) {
			allThreadMotor.set(0.4);
		}
		if(allThreadMotionUp == false) {
			allThreadMotor.set(0);
		}
		if((controller.getBButton() && controller.getRawAxis(3) == 1.0) && allThreadMotionDown == true) {
			allThreadMotor.set(-0.4);
		} 
		if(allThreadMotionDown == false) {
			allThreadMotor.set(0);
		} 
		if((controller.getXButton() && controller.getRawAxis(3) == 1.0) && allThreadMotionUp == true) {
			allThreadMotor.set(0.6);
		} 
//		if(allThreadMotionDown == false) {
//			allThreadMotor.set(0);
//		} 
		if((controller.getYButton() && controller.getRawAxis(3) == 1.0) && allThreadMotionDown == true) {
			allThreadMotor.set(-0.6);
		} 
//		if(allThreadMotionDown == false) {
//			allThreadMotor.set(0);
//		} 
//		else {
//			allThreadMotor.set(0);
//		}
		
//		if(controller.getAButton()) {
//			allThreadMotor.set(0.4);
//		} else if(controller.getBButton()) {
//			allThreadMotor.set(-0.4);
//		} else if(controller.getXButton()) {
//			allThreadMotor.set(0.6);
//		} else if(controller.getYButton()) {
//			allThreadMotor.set(-0.6);
//		} else {
//			allThreadMotor.set(0);
//		}
		
		//Cube Intake
		if(controller.getRawButton(6)) {
			clawMotor1.set(0.5);
			clawMotor2.set(0.5);
		} else if(controller.getRawButton(5)) {
			clawMotor1.set(-0.5);
			clawMotor2.set(-0.5);
		} else {
			clawMotor1.set(0);
			clawMotor2.set(0);
		}	
		
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
}
