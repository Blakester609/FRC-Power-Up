/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team4598.robot;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;

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
	
	//Victor rightDrive1 = new Victor(0);
	private Victor rightDrive1 = new Victor(0);
	private Victor rightDrive2 = new Victor(2);
	public SpeedControllerGroup rightSideDrive = new SpeedControllerGroup(rightDrive1, rightDrive2);
	
	//Victor leftDrive1 = new Victor(1);
	private Victor leftDrive1 = new Victor(1);
	private Victor leftDrive2 = new Victor(3);
	private SpeedControllerGroup leftSideDrive = new SpeedControllerGroup(leftDrive1, leftDrive2);
	
	
	private XboxController controller = new XboxController(1);
	
	private Victor climbingWinch = new Victor(5);
//	Victor liftMotor = new Victor(5);
	
	private TalonSRX liftMotor = new TalonSRX(0);
	
	int selSenPos = liftMotor.getSelectedSensorPosition(0);
	int pulseWidthWithoutOverflows = liftMotor.getSensorCollection().getPulseWidthPosition() & 0xFFF;
	
	private Victor clawMotor1 = new Victor(4);
	private Victor clawMotor2 = new Victor(6);
	private SpeedControllerGroup cubeIntake = new SpeedControllerGroup(clawMotor1, clawMotor2);
	
	private ADXRS450_Gyro gyro  = new ADXRS450_Gyro(SPI.Port.kOnboardCS0);
	
	public int autoCounter;
	

	String gameData;
	
	String autoCommand;
	
	private static final String red1 = AllianceStationID.Red1.toString();
	private static final String red2 = AllianceStationID.Red2.toString();
	private static final String red3 = AllianceStationID.Red3.toString();
	private static final String blue1 = AllianceStationID.Blue1.toString();
	private static final String blue2 = AllianceStationID.Blue2.toString();
	private static final String blue3 = AllianceStationID.Blue3.toString();
	private static final String switchAuto = "switch";
	private static final String scaleAuto = "scale";
	private String playerStation;
	private String scaleOrSwitch;
	private SendableChooser<String> playerStationChooser = new SendableChooser<>();
	private SendableChooser<String> switchOrScaleChooser = new SendableChooser<>();
	
	String autoCommand1 = "autonomous 1";
	String autoCommand2 = "autonomous 2";
	String autoCommand3 = "autonomous 3";
	String autoCommand4 = "autonomous 4";
	String autoCommand5 = "autonomous 5";
	String autoCommand6 = "autonomous 6";
	
	final int kTimeoutMs = 10;
	
	private boolean autoEnabled;
	private boolean teleOpEnabled;
	
	
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
		switchOrScaleChooser.addObject("Go for Switch", switchAuto);
		switchOrScaleChooser.addObject("Go for scale", scaleAuto);
		SmartDashboard.putData("Player Stations", playerStationChooser);
		SmartDashboard.putData("Switch or Scale?", switchOrScaleChooser);
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		autoCounter = 0;
		liftMotor.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, kTimeoutMs);
		liftMotor.setSelectedSensorPosition(0, 0, 10);
	}
	
// **************************************Autonomous Methods************************************
	private void visionTrackingRight() {
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
	
	private void visionTrackingLeft() {
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
	
	/* Drive to the left switch plate using the limelight camera when starting in the center
	 player station */
	private void rightSwitchWithLimelight() {
		autoCounter++;
		if(autoCounter < 125) {
			liftMotor.set(ControlMode.PercentOutput, 1.0);
		} else if(autoCounter > 125) {
			liftMotor.set(ControlMode.PercentOutput, 0);
		}
		if(autoCounter > 125 && autoCounter < 175) {
			leftSideDrive.set(0.4);
			rightSideDrive.set(0);
		} 
		gyro.reset();
		if(autoCounter > 175 && autoCounter < 225) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.01));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.03));
		}
		gyro.reset();
		if(autoCounter > 225 && autoCounter < 750) {
			visionTrackingLeft();
			if(ta.getDouble(0) >= 5.0) {
				cubeIntake.set(-1.0);
			}
		}
	}
	
	/* Drive to the right switch plate using the limelight camera when starting in the center
	player station */
	private void leftSwitchWithLimelight() {
		autoCounter++;
		if(autoCounter < 50 && autoCounter < 51) {
			liftMotor.set(ControlMode.PercentOutput, 1.0);
		} 
		if(autoCounter > 51 && autoCounter < 100) {
			leftSideDrive.set(0);
			rightSideDrive.set(-0.4);
		}
		gyro.reset();
		if(autoCounter > 100 && autoCounter < 200) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.015));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.015));
		} else if(autoCounter > 200 && autoCounter < 220) {
			leftSideDrive.set(0.4);
			rightSideDrive.set(0);
		}				
		gyro.reset();
		if(autoCounter > 200 && autoCounter < 750) {
			visionTrackingRight();
			if(ta.getDouble(0) >= 20.0) {
				cubeIntake.set(-1.0);
			}
		}
	}
	
	/* Drive to the left switch plate without using the Limelight camera when starting in the 
	 center player station */
	private void rightSwitchWithoutLimelight() {
		gyro.reset();
		autoCounter++;
		if(autoCounter < 120) {
			liftMotor.set(ControlMode.PercentOutput, -1.0);
		} else if(autoCounter > 120) {
			liftMotor.set(ControlMode.PercentOutput, -0.15);
		}
		
		if(autoCounter > 200 && autoCounter < 260) {
			leftSideDrive.set(0.4);
			rightSideDrive.set(0);
		}
		gyro.reset();
		if(autoCounter > 260 && autoCounter < 300) {
//			leftSideDrive.set(0.4+(gyro.getAngle()* 0.015));
//			rightSideDrive.set(-0.4+(gyro.getAngle()*0.015));
			leftSideDrive.set(0.4);
			rightSideDrive.set(-0.4);
		} else if(autoCounter > 300 && autoCounter < 340) {
			leftSideDrive.set(-0.35);
			rightSideDrive.set(-0.35);
		}				
		gyro.reset();
		if(autoCounter > 340 && autoCounter < 440) {
//			leftSideDrive.set(0.4+(gyro.getAngle()* 0.03));
//			rightSideDrive.set(-0.4+(gyro.getAngle()*0.1));
			leftSideDrive.set(0.4);
			rightSideDrive.set(-0.4);
		} else if(autoCounter > 440) {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		}
		gyro.reset();
		if(autoCounter > 440 && autoCounter < 500) {
			cubeIntake.set(-0.6);
		} else if(autoCounter > 500) {
			cubeIntake.set(0);
		}
	}
	
	/* Drive to the right switch plate without using the Limelight camera when starting in the 
		 center player station */
		private void leftSwitchWithoutLimelight() {
			gyro.reset();
			autoCounter++;
			if(autoCounter < 120) {
				liftMotor.set(ControlMode.PercentOutput, -1.0);
			} else if(autoCounter > 120) {
				liftMotor.set(ControlMode.PercentOutput, -0.15);
			}
			
			if(autoCounter > 200 && autoCounter < 260) {
				leftSideDrive.set(0);
				rightSideDrive.set(-0.4);
			}
			gyro.reset();
			if(autoCounter > 260 && autoCounter < 310) {
	//			leftSideDrive.set(0.4+(gyro.getAngle()* 0.015));
	//			rightSideDrive.set(-0.4+(gyro.getAngle()*0.015));
				leftSideDrive.set(0.4);
				rightSideDrive.set(-0.4);
			} else if(autoCounter > 310 && autoCounter < 360) {
				leftSideDrive.set(0.4);
				rightSideDrive.set(0);
			}				
			gyro.reset();
			if(autoCounter > 360 && autoCounter < 390) {
	//			leftSideDrive.set(0.4+(gyro.getAngle()* 0.03));
	//			rightSideDrive.set(-0.4+(gyro.getAngle()*0.1));
				leftSideDrive.set(0.4);
				rightSideDrive.set(-0.4);
			} else if(autoCounter > 390) {
				leftSideDrive.set(0);
				rightSideDrive.set(0);
			} 
			gyro.reset();
			if(autoCounter > 390 && autoCounter < 500) {
				cubeIntake.set(-0.6);
			} else if(autoCounter > 500) {
				cubeIntake.set(0);
			}
		}

	private void driveStraightLeft() {
		gyro.reset();
		autoCounter++;
		if(autoCounter < 20) {
			leftSideDrive.set(0.4);
			rightSideDrive.set(0);
		}
		gyro.reset();
		if(autoCounter > 20 && autoCounter < 70) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.03));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.1));
		} else if(autoCounter > 70 && autoCounter < 90) {
			leftSideDrive.set(0);
			rightSideDrive.set(-0.4);
		}
		gyro.reset();
		if(autoCounter > 90 && autoCounter < 300) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.03));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.1));
		} else if(autoCounter > 300) {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		} else {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		}

	}
	
	private void driveStraightRight() {
		gyro.reset();
		autoCounter++;
		if(autoCounter < 20) {
			leftSideDrive.set(0);
			rightSideDrive.set(-0.4);
		}
		gyro.reset();
		if(autoCounter > 20 && autoCounter < 70) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.03));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.1));
		} else if(autoCounter > 70 && autoCounter < 90) {
			leftSideDrive.set(4.0);
			rightSideDrive.set(0);
		}
		gyro.reset();
		if(autoCounter > 90 && autoCounter < 300) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.03));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.1));
		} else if(autoCounter > 300) {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		} else {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		}
	}
	
	private void driveStraight(int numIterations) {
		gyro.reset();
		autoCounter++;
		if(autoCounter < numIterations) {
			leftSideDrive.set(0.4);
			rightSideDrive.set(-0.5);
		} else if(autoCounter > numIterations) {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		}
	}
	
	private void driveStraightContinuing() {
			leftSideDrive.set(0.4);
			rightSideDrive.set(-0.5);
	}

	// Drive to the scale right plate when in the right player station
	private void scaleRightAndPlayerStationRight() {
		autoCounter++;
		if(autoCounter >= 0 && autoCounter <= 1) {
			gyro.reset();
		} else if(autoCounter >= 1 && autoCounter <= 251) {
			driveStraightContinuing();
		} else if(autoCounter >= 251 && autoCounter <= 280) {
			leftSideDrive.set(-0.4);
			rightSideDrive.set(-0.4);
		} else if(autoCounter >= 280) {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		} else if(autoCounter >= 280 && autoCounter <= 450) {
			liftToScaleAuto();
		} else if(autoCounter >= 450 && autoCounter <= 500) {
			cubeIntake.set(-0.6);
		} else if(autoCounter >= 500) {
			cubeIntake.set(0);
		} else if(autoCounter >= 500 && autoCounter <= 750) {
			liftDownAuto();
		}
	}

	// Drive to the scale left plate when in the left player station
	private void scaleLeftAndPlayerStationLeft() {
		autoCounter++;
		if(autoCounter >= 0 && autoCounter <= 1) {
			gyro.reset();
		} else if(autoCounter >= 1 && autoCounter <= 251) {
			driveStraightContinuing();
		} else if(autoCounter >= 251 && autoCounter <= 280) {
			leftSideDrive.set(0.4);
			rightSideDrive.set(0.4);
		} else if(autoCounter >= 280) {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		} else if(autoCounter >= 280 && autoCounter <= 450) {
			liftToScaleAuto();
		} else if(autoCounter >= 450 && autoCounter <= 500) {
			cubeIntake.set(-0.6);
		} else if(autoCounter >= 500) {
			cubeIntake.set(0);
		} else if(autoCounter >= 500 && autoCounter <= 750) {
			liftDownAuto();
		}
	}
	
	// Drive to the left scale plate when starting in the right player station
	private void scaleRightAndPlayerStationLeft() {
		gyro.reset();
		autoCounter++;
		if(autoCounter < 300) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.015));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.015));
		} else if(autoCounter > 310 && autoCounter < 350) {
			leftSideDrive.set(4.0);
			rightSideDrive.set(0);
		} 
		gyro.reset();
		if(autoCounter > 350 && autoCounter < 400) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.015));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.015));
		} else if(autoCounter > 400 & autoCounter < 420) {
			rightSideDrive.set(-4.0);
			leftSideDrive.set(0);
		} else if(autoCounter > 420) {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		} else if(autoCounter > 420 && autoCounter < 750) {
			
		} else if(autoCounter > 490 && autoCounter < 590) {
			liftMotor.set(ControlMode.PercentOutput, -1.0);
		} else if(autoCounter > 590) {
			liftMotor.set(ControlMode.PercentOutput, 0);
		} else if(autoCounter > 590 && autoCounter < 625) {
			cubeIntake.set(-0.6);
		} else if(autoCounter > 625) {
			cubeIntake.set(0);
		}
	}
	
	// Drive to the right scale plate when starting in the left player station
	private void scaleLeftAndPlayerStationRight() {
		gyro.reset();
		autoCounter++;
		if(autoCounter < 300) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.015));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.015));
		} else if(autoCounter > 310 && autoCounter < 350) {
			leftSideDrive.set(0);
			rightSideDrive.set(-4.0);
		} 
		gyro.reset();
		if(autoCounter > 350 && autoCounter < 400) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.015));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.15));
		} else if(autoCounter > 400 & autoCounter < 420) {
			rightSideDrive.set(4.0);
			leftSideDrive.set(0);
		} else if(autoCounter > 420) {
			leftSideDrive.set(0);
			rightSideDrive.set(0);			
		} else if(autoCounter > 420 && autoCounter < 750) {
			
		} else if(autoCounter > 490 && autoCounter < 590) {
			liftMotor.set(ControlMode.PercentOutput, -1.0);
		} else if(autoCounter > 590) {
			liftMotor.set(ControlMode.PercentOutput, 0);
		} else if(autoCounter > 590 && autoCounter < 625) {
			cubeIntake.set(-0.6);
		} else if(autoCounter > 625) {
			cubeIntake.set(0);
		}
		
	}
	

	
	private void switchRightAndPlayerStationRight() {
		autoCounter++;
		if(autoCounter >= 0 && autoCounter <= 1) {
			gyro.reset();
		}
		if(autoCounter < 120) {
			liftMotor.set(ControlMode.PercentOutput, -1.0);
		} else if(autoCounter > 120) {
			liftMotor.set(ControlMode.PercentOutput, -0.15);
		}
		else if(autoCounter >= 120 && autoCounter <= 270) {
			driveStraightContinuing();
		} else if(autoCounter >= 270 && autoCounter <= 300) {
			leftSideDrive.set(-0.4);
			rightSideDrive.set(-0.4);
		} else if(autoCounter >= 300) {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		} else if(autoCounter >= 300) {
			
		}
		
		gyro.reset();
		autoCounter++;
		if(autoCounter < 20) {
			leftSideDrive.set(0.4);
			rightSideDrive.set(0);
		} 
		gyro.reset();
		if(autoCounter > 20 && autoCounter < 100) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.015));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.15));
		} else if(autoCounter > 100 && autoCounter < 130) {
			leftSideDrive.set(0);
			rightSideDrive.set(-0.4);
		}
		gyro.reset();
		if(autoCounter > 130 && autoCounter < 140) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.015));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.15));
		} else if(autoCounter > 140) {
			leftSideDrive.set(0);
		}
		else if(autoCounter > 140 && autoCounter < 190) {
			liftMotor.set(ControlMode.PercentOutput, -1.0);
		} else if(autoCounter > 190) {
			liftMotor.set(ControlMode.PercentOutput, 0);
		} else if(autoCounter > 190 && autoCounter < 240) {
			cubeIntake.set(-0.6);
		}
	}
	
	private void switchLeftAndPlayerStationLeft() {
		
		gyro.reset();
		autoCounter++;
		if(autoCounter < 20) {
			leftSideDrive.set(0);
			rightSideDrive.set(-0.4);
		} 
		gyro.reset();
		if(autoCounter > 20 && autoCounter < 100) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.015));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.15));
		} else if(autoCounter > 100 && autoCounter < 130) {
			leftSideDrive.set(0.4);
			rightSideDrive.set(0);
		}
		gyro.reset();
		if(autoCounter > 130 && autoCounter < 140) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.015));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.15));
		} else if(autoCounter > 140) {
			leftSideDrive.set(0);
		}
		else if(autoCounter > 140 && autoCounter < 190) {
			liftMotor.set(ControlMode.PercentOutput, -1.0);
		} else if(autoCounter > 190) {
			liftMotor.set(ControlMode.PercentOutput, 0);
		} else if(autoCounter > 190 && autoCounter < 240) {
			cubeIntake.set(-0.6);
		}
	}
	
	private void switchRightAndPlayerStationLeft() {
		gyro.reset();
		autoCounter++;
		if(autoCounter < 20) {
			leftSideDrive.set(0);
			rightSideDrive.set(-0.4);
		} 
		gyro.reset();
		if(autoCounter > 20 && autoCounter < 100) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.015));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.15));
		}
	}
	
	
	
//^^^^^^^^^^^^^^^^^^^^^^^^^Autonomous Methods^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

//************************************Control methods***********************************************//
	private void clawIdleIn() {
		cubeIntake.set(0.15);
	}
	
	private void liftToScaleAuto() {
		if(selSenPos <= 68000) {
			liftMotor.set(ControlMode.PercentOutput, -1.0);
		} else if(selSenPos >= 68000) {
			liftMotor.set(ControlMode.PercentOutput, -0.15);
		}
	}
	
	private void liftDownAuto() {
		if(selSenPos >= 0) {
			liftMotor.set(ControlMode.PercentOutput, 1.0);
		} else if(selSenPos <= 0) {
			liftMotor.set(ControlMode.PercentOutput, 0);
		}
	}
	
	
	
//^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^Control Methods^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^//
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
		gameData = DriverStation.getInstance().getGameSpecificMessage();
		playerStation = playerStationChooser.getSelected();
		scaleOrSwitch = switchOrScaleChooser.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
//		playerStation = blue2;
		System.out.println("Player Station: " + playerStation);
		if(playerStation == blue2 || playerStation == red2) {
			if(gameData.length() > 0) {
				if(gameData.charAt(0) == 'R') {
						autoCommand = "autonomous 1";
				}
				if(gameData.charAt(0) == 'L') {
					autoCommand = "autonomous 2";
				}
			}
		} else if((playerStation == blue3 || playerStation == red3) && scaleOrSwitch == scaleAuto) {
			if(gameData.charAt(1) == 'L') {
				autoCommand = "autonomous 3";
			}
			if(gameData.charAt(1) == 'R') {
				autoCommand = "autonomous 4";
			}
		} else if((playerStation == blue3 || playerStation == red3) && scaleOrSwitch == switchAuto) {
			if(gameData.charAt(0) == 'L') {
				autoCommand = "autonomous 7";
			}
			if(gameData.charAt(0) == 'R') {
				autoCommand = "autonomous 8";
			}
		} else if((playerStation == blue1 || playerStation == red1) && scaleOrSwitch == scaleAuto) {
			if(gameData.charAt(1) == 'L') {
				autoCommand = "autonomous 5";
			}
			if(gameData.charAt(1) == 'R') {
				autoCommand = "autonomous 6";
			}
		} else if((playerStation == blue1 || playerStation == red1) && scaleOrSwitch == switchAuto) {
			if(gameData.charAt(1) == 'L') {
				autoCommand = "autonomous 9";
			}
			if(gameData.charAt(1) == 'R') {
				autoCommand = "autonomous 10";
			}
		}
		
		
		
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		autoEnabled = true;
		teleOpEnabled = false;
		ledMode.setNumber(1);
		camMode.setNumber(1);
		switch (autoCommand) {
			case "autonomous 1":
//				rightSwitchWithLimelight();
				rightSwitchWithoutLimelight();
				break;
			case "autonomous 2":
//				leftSwitchWithLimelight();
				leftSwitchWithoutLimelight();
				break;
			case "autonomous 3":
//				driveStraightRight();
//				scaleLeftAndPlayerStationRight();
				driveStraight(150);
				break;
			case "autonomous 4":
//				driveStraightRight();
				scaleRightAndPlayerStationRight();
//				driveStraight(150);
				break;
			case "autonomous 5":
//				driveStraightLeft();
//				scaleLeftAndPlayerStationLeft();
				driveStraight(150);
				break;
			case "autonomous 6":
//				scaleRightAndPlayerStationLeft();
//				driveStraightLeft();
				driveStraight(150);
				break;
			case "autonomous 7":
				break;
			case "autonomous 8":
				switchRightAndPlayerStationRight();
				break;
			case "autonomous 9":
				switchLeftAndPlayerStationLeft();
				break;
			case "autonomous 10":
				break;
			}
	}
		


	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		int selSenPos = liftMotor.getSelectedSensorPosition(0);
		int pulseWidthWithoutOverflows = liftMotor.getSensorCollection().getPulseWidthPosition() & 0xFFF;
		
		double leftStickY = controller.getRawAxis(1);
		double leftStickX = controller.getRawAxis(0);
		double rightStickY = controller.getRawAxis(5);
		double leftTrigger = controller.getRawAxis(2);
		double rightTrigger = controller.getRawAxis(3);
		
		autoEnabled = false;
		teleOpEnabled = true;
		
		SmartDashboard.putNumber("pulseWidthPosition", pulseWidthWithoutOverflows);
		SmartDashboard.putNumber("selSenPos", selSenPos);
		
		ledMode.setNumber(1);
		camMode.setNumber(1);
		// Drive
		double reduceLeftSpeed = 0;
		double reduceRightSpeed = 0;
		
		if(leftStickY < -0.01) {
			reduceLeftSpeed += leftStickX*0.225;
			reduceRightSpeed += leftStickX*0.225;
		} else if(leftStickY > 0.01) {
			reduceLeftSpeed -= leftStickX*0.225;
			reduceRightSpeed -= leftStickX*0.225;
		} else {
			reduceLeftSpeed = 0;
			reduceRightSpeed = 0;
		}
		
		if(leftStickY != 0) {
			double leftSpeed = -0.7*leftStickY+reduceLeftSpeed;
			double rightSpeed = 0.7*leftStickY+reduceRightSpeed;	
			leftSideDrive.set(leftSpeed);
			rightSideDrive.set(rightSpeed);
		} 
		else {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		}
		// Dime turn
		if(controller.getRawButton(5)) {
			leftSideDrive.set(-0.45);
			rightSideDrive.set(-0.45);

		} else if(controller.getRawButton(6)) {
			leftSideDrive.set(0.45);
			rightSideDrive.set(0.45);

		}

		
		if(rightTrigger > 0.15) {
			cubeIntake.set(rightTrigger);
		} else if(leftTrigger > 0.15) {
			cubeIntake.set(-leftTrigger);
		} else {
			clawIdleIn();
		}
		
		
		
		SmartDashboard.putNumber("RawAxis5", rightStickY);
		
		if(selSenPos < 0 && (rightStickY > 0.1 || (rightStickY >= -0.1 && rightStickY < 0.1))) {
			liftMotor.set(ControlMode.PercentOutput, 0);
		} else if((selSenPos >= -40000 && selSenPos <= 0) && rightStickY < 0.1) {
				liftMotor.set(ControlMode.PercentOutput, rightStickY);
		} else if(selSenPos >= 0  && (rightStickY <= -0.1 || rightStickY >= 0.1)) {
			liftMotor.set(ControlMode.PercentOutput, rightStickY);
		} 
		else if(selSenPos > 65000  && (rightStickY < 0.1 || (rightStickY >= -0.1 && rightStickY < 0.1))) {
			liftMotor.set(ControlMode.PercentOutput, -0.15);
		} 
		else if((selSenPos >= 68000 && selSenPos <= 8000000) && rightStickY > -0.1) {
			liftMotor.set(ControlMode.PercentOutput, rightStickY);
		} 
		else if((selSenPos > 10000 && selSenPos < 20000) && (rightStickY > -0.1 && rightStickY < 0.1)) {
			liftMotor.set(ControlMode.PercentOutput, -0.1);
		} else if(selSenPos > 20000 && (rightStickY > -0.1 && rightStickY < 0.1)) {
			liftMotor.set(ControlMode.PercentOutput, -0.15);
		}
		
		
		if(controller.getAButton()) {
			climbingWinch.set(1.0);
		} else if(controller.getBButton()) {
			climbingWinch.set(-1.0);
		} else {
			climbingWinch.set(0);
		}
		
		
		
		
			
		
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
		driveStraightContinuing();
	}
}
