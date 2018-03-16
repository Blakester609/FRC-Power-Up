/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team4598.robot;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.ControlMode;

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
	Victor rightDrive1 = new Victor(0);
	Victor rightDrive2 = new Victor(2);
	public SpeedControllerGroup rightSideDrive = new SpeedControllerGroup(rightDrive1, rightDrive2);
	
	//Victor leftDrive1 = new Victor(1);
	Victor leftDrive1 = new Victor(1);
	Victor leftDrive2 = new Victor(3);
	public SpeedControllerGroup leftSideDrive = new SpeedControllerGroup(leftDrive1, leftDrive2);
	
	
	XboxController controller = new XboxController(1);
	
	Victor allThreadMotor = new Victor(9);
//	Victor winchMotor = new Victor(5);
	
	private TalonSRX winchMotor = new TalonSRX(0);
	
	Victor clawMotor1 = new Victor(4);
	Victor clawMotor2 = new Victor(6);
	
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
	}
	
// **************************************Autonomous Methods************************************
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
	
	/* Drive to the left switch plate using the limelight camera when starting in the center
	 player station */
	private void rightSwitchWithLimelight() {
		autoCounter++;
		if(autoCounter < 125) {
			winchMotor.set(ControlMode.PercentOutput, 1.0);
		} else if(autoCounter > 125) {
			winchMotor.set(ControlMode.PercentOutput, 0);
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
				clawMotor1.set(-1.0);
				clawMotor2.set(-1.0);
			}
		}
	}
	
	/* Drive to the right switch plate using the limelight camera when starting in the center
	player station */
	private void leftSwitchWithLimelight() {
		autoCounter++;
		if(autoCounter < 50 && autoCounter < 51) {
			winchMotor.set(ControlMode.PercentOutput, 1.0);
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
				clawMotor1.set(1.0);
				clawMotor2.set(-1.0);
			}
		}
	}
	
	/* Drive to the left switch plate without using the Limelight camera when starting in the 
	 center player station */
	private void rightSwitchWithoutLimelight() {
		gyro.reset();
		autoCounter++;
		if(autoCounter < 120) {
			winchMotor.set(ControlMode.PercentOutput, -1.0);
		} else if(autoCounter > 120) {
			winchMotor.set(ControlMode.PercentOutput, 0.15);
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
			clawMotor1.set(-0.6);
			clawMotor2.set(-0.6);
		} else if(autoCounter > 500) {
			clawMotor1.set(0);
			clawMotor2.set(0);
		}
	}
	
	/* Drive to the right switch plate without using the Limelight camera when starting in the 
		 center player station */
		private void leftSwitchWithoutLimelight() {
			gyro.reset();
			autoCounter++;
			if(autoCounter < 120) {
				winchMotor.set(ControlMode.PercentOutput, -1.0);
			} else if(autoCounter > 120) {
				winchMotor.set(ControlMode.PercentOutput, 0.15);
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
				clawMotor1.set(-0.6);
				clawMotor2.set(-0.6);
			} else if(autoCounter > 500) {
				clawMotor1.set(0);
				clawMotor2.set(0);
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
	
	private void driveStraight() {
		gyro.reset();
		autoCounter++;
		if(autoCounter < 150) {
			leftSideDrive.set(0.4);
			rightSideDrive.set(-0.4);
		} else if(autoCounter > 150) {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		}
	}

	// Drive to the scale right plate when in the right player station
	private void scaleRightAndPlayerStationRight() {
		gyro.reset();
		autoCounter++;
		if(autoCounter < 20) {
			leftSideDrive.set(0.4);
			rightSideDrive.set(0);
		}
		gyro.reset();
		if (autoCounter > 20 && autoCounter < 70) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.03));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.1));
		} else if(autoCounter > 70 && autoCounter < 90) {
			leftSideDrive.set(0);
			rightSideDrive.set(-0.4);
		} 
		gyro.reset();
		if(autoCounter > 90 && autoCounter < 350) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.03));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.1));
		} else if(autoCounter > 350 && autoCounter < 365) {
			leftSideDrive.set(0);
			rightSideDrive.set(-0.4);
		} 
		gyro.reset();
		if(autoCounter > 365 && autoCounter < 385) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.03));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.1));
		}
		else if(autoCounter > 385) {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		} else if(autoCounter > 385 && autoCounter < 750) {
			limitSwitchesAllThreads();
			if(limitAllThreadDown != null && limitAllThreadDown != null) {
				if(allThreadMotionUp == true) {
					allThreadMotor.set(1.0);
				} else if(allThreadMotionDown == true) {
					allThreadMotor.set(-1.0);
				} else if(allThreadMotionUp == false) {
					allThreadMotor.set(0);
				} else if(allThreadMotionDown == false) {
					allThreadMotor.set(0);
				}
			} else if(limitAllThreadDown == null || limitAllThreadUp == null) {
				if(autoCounter > 385 && autoCounter < 455) {
					allThreadMotor.set(1.0);
				} else if(autoCounter > 455) {
					allThreadMotor.set(0);
				}
			}
		}
		else if(autoCounter > 600 && autoCounter < 650) {
			winchMotor.set(ControlMode.PercentOutput, -1.0);
		} else if(autoCounter > 650) {
			winchMotor.set(ControlMode.PercentOutput, 0);
		} else if(autoCounter > 650 && autoCounter < 700) {
			clawMotor1.set(-1.0);
			clawMotor2.set(-1.0);
		}
	}
	
	// Drive to the scale left plate when in the left player station
	private void scaleLeftAndPlayerStationLeft()
	 {
		gyro.reset();
		autoCounter++;
		if(autoCounter < 20) {
			leftSideDrive.set(0);
			rightSideDrive.set(-0.4);
		}
		gyro.reset();
		if (autoCounter > 20 && autoCounter < 70) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.03));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.1));
		} else if(autoCounter > 70 && autoCounter < 90) {
			leftSideDrive.set(0.4);
			rightSideDrive.set(0);
		} 
		gyro.reset();
		if(autoCounter > 90 && autoCounter < 350) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.03));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.1));
		} else if(autoCounter > 350 && autoCounter < 365) {
			leftSideDrive.set(4.0);
			rightSideDrive.set(0);
		} 
		gyro.reset();
		if(autoCounter > 365 && autoCounter < 385) {
			leftSideDrive.set(0.4+(gyro.getAngle()* 0.03));
			rightSideDrive.set(-0.4+(gyro.getAngle()*0.1));
		}
		else if(autoCounter > 385) {
			leftSideDrive.set(0);
			rightSideDrive.set(0);
		} 
		else if(autoCounter > 385 && autoCounter < 750) {
			limitSwitchesAllThreads();
			if(limitAllThreadDown != null && limitAllThreadDown != null) {
				if(allThreadMotionUp == true) {
					allThreadMotor.set(1.0);
				} else if(allThreadMotionDown == true) {
					allThreadMotor.set(-1.0);
				} else if(allThreadMotionUp == false) {
					allThreadMotor.set(0);
				} else if(allThreadMotionDown == false) {
					allThreadMotor.set(0);
				}
			} else if(limitAllThreadDown == null || limitAllThreadUp == null) {
				if(autoCounter > 385 && autoCounter < 455) {
					allThreadMotor.set(1.0);
				} else if(autoCounter > 455) {
					allThreadMotor.set(0);
				}
			}
		}
		else if(autoCounter > 600 && autoCounter < 650) {
			winchMotor.set(ControlMode.PercentOutput, -1.0);
		} else if(autoCounter > 650) {
			winchMotor.set(ControlMode.PercentOutput, 0);
		} else if(autoCounter > 650 && autoCounter < 700) {
			clawMotor1.set(-1.0);
			clawMotor2.set(-1.0);
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
			limitSwitchesAllThreads();
			if(limitAllThreadDown != null && limitAllThreadDown != null) {
				if(allThreadMotionUp == true) {
					allThreadMotor.set(1.0);
				} else if(allThreadMotionDown == true) {
					allThreadMotor.set(-1.0);
				} else if(allThreadMotionUp == false) {
					allThreadMotor.set(0);
				} else if(allThreadMotionDown == false) {
					allThreadMotor.set(0);
				}
			} else if(limitAllThreadDown == null || limitAllThreadUp == null) {
				if(autoCounter > 420 && autoCounter < 490) {
					allThreadMotor.set(1.0);
				} else if(autoCounter > 490) {
					allThreadMotor.set(0);
				}
			}
		} else if(autoCounter > 490 && autoCounter < 590) {
			winchMotor.set(ControlMode.PercentOutput, -1.0);
		} else if(autoCounter > 590) {
			winchMotor.set(ControlMode.PercentOutput, 0);
		} else if(autoCounter > 590 && autoCounter < 625) {
			clawMotor1.set(-1.0);
			clawMotor2.set(-1.0);
		} else if(autoCounter > 625) {
			clawMotor1.set(0);
			clawMotor2.set(0);
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
			limitSwitchesAllThreads();
			if(limitAllThreadDown != null && limitAllThreadDown != null) {
				if(allThreadMotionUp == true) {
					allThreadMotor.set(1.0);
				} else if(allThreadMotionDown == true) {
					allThreadMotor.set(-1.0);
				} else if(allThreadMotionUp == false) {
					allThreadMotor.set(0);
				} else if(allThreadMotionDown == false) {
					allThreadMotor.set(0);
				}
			} else if(limitAllThreadDown == null || limitAllThreadUp == null) {
				if(autoCounter > 420 && autoCounter < 490) {
					allThreadMotor.set(1.0);
				} else if(autoCounter > 490) {
					allThreadMotor.set(0);
				}
			}
		} else if(autoCounter > 490 && autoCounter < 590) {
			winchMotor.set(ControlMode.PercentOutput, -1.0);
		} else if(autoCounter > 590) {
			winchMotor.set(ControlMode.PercentOutput, 0);
		} else if(autoCounter > 590 && autoCounter < 625) {
			clawMotor1.set(-1.0);
			clawMotor2.set(-1.0);
		} else if(autoCounter > 625) {
			clawMotor1.set(0);
			clawMotor2.set(0);
		}
		
	}
	
	private void limitSwitchesAllThreads() {
		if(limitAllThreadUp.get()) {
			allThreadMotionUp = false;
		} else if(!limitAllThreadUp.get()) {
			allThreadMotionUp = true;
		}
		
		if(limitAllThreadDown.get()) {
			allThreadMotionDown = false;
		} else if(!limitAllThreadDown.get()) {
			allThreadMotionDown = true;
		}
	}
	
	private void switchRightAndPlayerStationRight() {
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
			winchMotor.set(ControlMode.PercentOutput, -1.0);
		} else if(autoCounter > 190) {
			winchMotor.set(ControlMode.PercentOutput, 0);
		} else if(autoCounter > 190 && autoCounter < 240) {
			clawMotor1.set(-1.0);
			clawMotor2.set(-1.0);
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
			winchMotor.set(ControlMode.PercentOutput, -1.0);
		} else if(autoCounter > 190) {
			winchMotor.set(ControlMode.PercentOutput, 0);
		} else if(autoCounter > 190 && autoCounter < 240) {
			clawMotor1.set(-1.0);
			clawMotor2.set(-1.0);
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
				driveStraight();
				break;
			case "autonomous 4":
//				driveStraightRight();
//				scaleRightAndPlayerStationRight();
				driveStraight();
				break;
			case "autonomous 5":
//				driveStraightLeft();
//				scaleLeftAndPlayerStationLeft();
				driveStraight();
				break;
			case "autonomous 6":
//				scaleRightAndPlayerStationLeft();
//				driveStraightLeft();
				driveStraight();
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
		
		ledMode.setNumber(1);
		camMode.setNumber(1);
		// Drive
		double reduceLeftSpeed = 0;
		double reduceRightSpeed = 0;
		
		if(controller.getRawAxis(1) < -0.01) {
			reduceLeftSpeed += controller.getRawAxis(0)*0.225;
			reduceRightSpeed += controller.getRawAxis(0)*0.225;
		} else if(controller.getRawAxis(1) > 0.01) {
			reduceLeftSpeed -= controller.getRawAxis(0)*0.225;
			reduceRightSpeed -= controller.getRawAxis(0)*0.225;
		} else {
			reduceLeftSpeed = 0;
			reduceRightSpeed = 0;
		}
		
		if(controller.getRawAxis(1) != 0) {
			double leftSpeed = -0.7*controller.getRawAxis(1)+reduceLeftSpeed;
			double rightSpeed = 0.7*controller.getRawAxis(1)+reduceRightSpeed;	
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
		
		
		// Winch Control
//		if(controller.getAButton()) {
//			winchMotor.set(-1.0);
//		} else if(controller.getBButton()) {
//			winchMotor.set(1.0);
//		} else if(controller.getXButton()) {
//			winchMotor.set(-0.6);
//		} else if(controller.getYButton()) {
//			winchMotor.set(0.6);
//		} else {
//			winchMotor.set(0);
//		}
//		
//		limitSwitchesAllThreads();
		
		
		
		// All Thread Control
//		if((controller.getAButton() && controller.getRawAxis(3) == 1.0) && allThreadMotionUp == true) {
//			allThreadMotor.set(0.4); 
//		} else if((controller.getBButton() && controller.getRawAxis(3) == 1.0) && allThreadMotionDown == true) {
//			allThreadMotor.set(-0.4);
//		} else
//		if(limitAllThreadDown != null && limitAllThreadUp != null) {
//			if((controller.getXButton()) && allThreadMotionUp == true) {
//				allThreadMotor.set(1.0);
//			} else if((controller.getYButton()) && allThreadMotionDown == true) {
//				allThreadMotor.set(-1.0);
//			} else if(allThreadMotionDown == false) {
//				allThreadMotor.set(0);
//			} else if(allThreadMotionUp == false) {
//				allThreadMotor.set(0);
//			} else {
//				allThreadMotor.set(0);
//			}
//		} else if(limitAllThreadDown == null || limitAllThreadUp == null) {
//			if(controller.getXButton()) {
//				allThreadMotor.set(1.0);
//			} else if(controller.getYButton()) {
//				allThreadMotor.set(-1.0);
//			} else {
//				allThreadMotor.set(0);
//			}
//		}
		
//		if(limitAllThreadDown != null && limitAllThreadUp != null) {
//			if((controller.getXButton()) && allThreadMotionUp == true) {
//				allThreadMotor.set(0.6);
//			} else if((controller.getYButton()) && allThreadMotionDown == true) {
//				allThreadMotor.set(-0.6);
//			} else if(allThreadMotionDown == false) {
//				allThreadMotor.set(0);
//			} else if(allThreadMotionUp == false) {
//				allThreadMotor.set(0);
//			} else {
//				allThreadMotor.set(0);
//			}
//		} else if(limitAllThreadDown == null || limitAllThreadUp == null) {
//			if(controller.getXButton()) {
//				allThreadMotor.set(0.7);
//			} else if(controller.getYButton()) {
//				allThreadMotor.set(-1.0);
//			} else {
//				allThreadMotor.set(0);
//			}
//		}
		
//		if(limitAllThreadDown != null && limitAllThreadUp != null) {
//			if((controller.getRawAxis(5) > 0.1) && allThreadMotionDown == true) {
//				allThreadMotor.set(-controller.getRawAxis(5));
//			} else if((controller.getRawAxis(5) < 0.1) && allThreadMotionUp == true) {
//				allThreadMotor.set(-controller.getRawAxis(5));
//			} else if(allThreadMotionDown == false) {
//				allThreadMotor.set(0);
//			} else if(allThreadMotionUp == false) {
//				allThreadMotor.set(0);
//			} else {
//				allThreadMotor.set(0);
//			}
//		} else if(limitAllThreadDown == null || limitAllThreadUp == null) {
//			if(controller.getRawAxis(5) > 0.1) {
//				allThreadMotor.set(-controller.getRawAxis(5));
//			} else if(controller.getRawAxis(5) < 0.1) {
//				allThreadMotor.set(-controller.getRawAxis(5));
//			} else {
//				allThreadMotor.set(0);
//			}
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
//		if(controller.getRawAxis(2) == 1.0) {
//			clawMotor1.set(1.0);
//			clawMotor2.set(1.0);
//		} else if(controller.getRawAxis(3) == 1.0) {
//			clawMotor1.set(-1.0);
//			clawMotor2.set(-1.0);
//		} else {
//			clawMotor1.set(0);
//			clawMotor2.set(0);
//		}	
		
		if(controller.getRawAxis(3) > 0.1) {
			clawMotor1.set(controller.getRawAxis(3));
			clawMotor2.set(controller.getRawAxis(3));
		} else if(controller.getRawAxis(2) > 0.1) {
			clawMotor1.set(-controller.getRawAxis(2));
			clawMotor2.set(-controller.getRawAxis(2));
		} else {
			clawMotor1.set(0);
			clawMotor2.set(0);
		}
		
		if(controller.getRawAxis(5) > 0.1 || controller.getRawAxis(5) < -0.1) {
			winchMotor.set(ControlMode.PercentOutput, -controller.getRawAxis(5));
		} else if(controller.getRawAxis(5) > -0.1 && controller.getRawAxis(5) < 0.1) {
			winchMotor.set(ControlMode.PercentOutput, 0.15);
		}
		
		
			
		
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
		
	}
}
