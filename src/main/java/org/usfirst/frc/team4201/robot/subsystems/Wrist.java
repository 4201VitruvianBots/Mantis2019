
package org.usfirst.frc.team4201.robot.subsystems;

import org.usfirst.frc.team4201.robot.Robot;
import org.usfirst.frc.team4201.robot.RobotMap;
import org.usfirst.frc.team4201.robot.LUTs;
import org.usfirst.frc.team4201.robot.commands.UpdateWristSetpoint;
import org.usfirst.frc.team4201.robot.interfaces.Shuffleboard;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.command.PIDSubsystem;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Wrist extends PIDSubsystem {
	static double kP = 0.03;		// Test values for Triple Threat
	static double kI = 0;
	static double kD = 0.00;
	static double kF = 0;
	static double period = 0.01;
	
	public static int armLimiterLowerBound = -46;
	public static int armLimiterUpperBound = 46;	

	public double angleLowerLimit = -125;													// -75
	public double angleUpperLimit = 150;													// 50 	
	public double sensorLowerLimit = 0;														//-133;
	public double sensorUpperLimit = -1080; 	// Negative value to 'invert' sensor		// 80; 
	// To recalibrate wrist pot:
	// 1. Look at wrist vavlue on Shuffleboard when wrist is parallel to ground
	// 2. Determine adjustment value (Shuffleboard value * 2.33)
	// 3. Add this to sensor offset
	static double sensorOffset = 356; //462; 			// 726 //431	//563;	//356
	static double voltageLowerLimit = 0;
	static double voltageUpperLimit = 5;

	public WPI_TalonSRX wristMotor = new WPI_TalonSRX(RobotMap.wristMotor);
	public AnalogInput wP = new AnalogInput(RobotMap.wristPot);
	public AnalogPotentiometer wristPot = new AnalogPotentiometer(wP, sensorUpperLimit, sensorOffset);
	
	/*	Wrist Values:
	 *	60 (0): Parallel to Ground 
	 *	180 = 120 + 60 (130): Parallel to arm (I use 130 because it seems better in practice)
	 *	105 (45): Shoot Forward
	 *	240 (180): Reverse Parallel
	 *	195 (135): Reverse Shoot
	 */
	
	
	public Wrist() {
		super("Wrist", kP, kI, kD, kF, period);
		setAbsoluteTolerance(8);
		setInputRange(angleLowerLimit, angleUpperLimit);
		setOutputRange(-1, 1);
		
		wristMotor.setNeutralMode(NeutralMode.Brake);
		wristMotor.configPeakOutputForward(1, 0);
		wristMotor.configPeakOutputReverse(-1, 0);
		//wristMotor.setSafetyEnabled(true);
		wristMotor.configContinuousCurrentLimit(20, 0);
		wristMotor.configPeakCurrentLimit(45, 0);
		wristMotor.configPeakCurrentDuration(100, 0);
		wristMotor.enableCurrentLimit(true);
		
		// Initialize the setpoint to where the wrist starts so it doesn't move
		setSetpoint(getAbsoluteAngle());
		
		// Enable the PIDController if state == 0
		if(RobotMap.WristState == 0)
			enable();
		
		// Add the PIDController to LiveWindow
		LiveWindow.addChild(this, this);
		
		wristPot.setName("Potentiometer");
		wristPot.setSubsystem("Wrist");
        LiveWindow.add(wristPot);
        
        wristMotor.setName("Wrist Motor");
        wristMotor.setSubsystem("Wrist");
        LiveWindow.add(wristMotor);
	}
	
	// Get the angle of the wrist directly from the motor
	public double getUnscaledAngle() {
		return wristPot.get();
	}
	
	// Get the angle of the wrist at the sprocket
	public double getAbsoluteAngle() {
		// Note: Sensor noise of +/- 5 degrees seems to be okay
		return wristPot.get() / 2.333; // 2.333:1 gear ratio
		//return (wristPot.getAverageVoltage() * ((sensorUpperLimit - sensorLowerLimit)/(voltageUpperLimit - voltageLowerLimit))) + sensorOffset;
	}
	
	// Get the angle of the wrist based off of the angle of the arm
	public double getRelativeAngle() {
		return getAbsoluteAngle() + Robot.arm.getAngle() + 60;
	}
	
	// Get the angle of the wrist based off of the angle of the arm
	public double getArmRelativeAngle() {
		return Robot.arm.getAngle() + 60;
	}
	
	public double convertRelativeToAbsoluteSetpoint(double value) {
		return value - getArmRelativeAngle();
	}
	
	public double getValidAngle(double value){
		if(value > angleLowerLimit && value < angleUpperLimit)
			if(Math.abs(Robot.arm.getAngle()) <= 50) {
				double angleLimit = LUTs.wristLimits[(int)Math.ceil(Robot.arm.getAngle()) - armLimiterLowerBound];
				
				if(Math.abs(Robot.wrist.getAbsoluteAngle()) < angleLimit) {
					if(Robot.wrist.getAbsoluteAngle() < 0)
						return -angleLimit;
					else
						return angleLimit;
				}
				else
					return value;
			} else
				return value;
		else if(value > angleUpperLimit)
			return angleUpperLimit - 2;
		else if(value < angleLowerLimit)
			return angleLowerLimit + 2;
		else
			return -500;	// Error value. Should never be returned.
	}
	
	public boolean checkLimits(double value){
		// check if the value is bound by the hard limits
		if(value > angleLowerLimit && value < angleUpperLimit)
			return true;
		else
			return false;
	}
	
	public void setMotorsToBrake(){
		wristMotor.setNeutralMode(NeutralMode.Brake);
	}
	
	public void setMotorsToCoast(){
		wristMotor.setNeutralMode(NeutralMode.Coast);
	}
	
	public void setDirectOutput(double output){
		wristMotor.set(ControlMode.PercentOutput, output);
	}
	
	@Override
	protected double returnPIDInput() {
		return getAbsoluteAngle();
	}

	@Override
	protected void usePIDOutput(double output) {
		// TODO Auto-generated method stub
		wristMotor.set(ControlMode.PercentOutput, output);
	}
	
	public double getSensorOffset() {
		return Wrist.sensorOffset;
	}
	
	public void setSensorOffset(double sensorOffset) {
		Wrist.sensorOffset = sensorOffset;
		wristPot.free();
		wristPot = new AnalogPotentiometer(wP, sensorUpperLimit, sensorOffset);
	}
	
	public void initializeSmartDashboard() {
		SmartDashboard.putNumber("Wrist Sensor Offset", Wrist.sensorOffset);
	}
	
	public void updateSmartDashboard() {
		// Use Shuffleboard to place things in their own tabs
		Shuffleboard.putNumber("Wrist", "Absolute Angle", getAbsoluteAngle());
		Shuffleboard.putNumber("Wrist", "Relative Angle", getRelativeAngle());
		Shuffleboard.putNumber("Wrist", "Motor Angle", getUnscaledAngle());
		Shuffleboard.putNumber("Wrist", "Setpoint", getPIDController().getSetpoint());
		Shuffleboard.putNumber("Wrist", "Pot Avg. Voltage", wP.getAverageVoltage());
		Shuffleboard.putNumber("Wrist", "Arm Angle", Robot.arm.getAngle());
		Shuffleboard.putBoolean("Wrist", "PID Enabled", getPIDController().isEnabled());
		Shuffleboard.putBoolean("Wrist", "On Target", onTarget());
		
		try {
			Shuffleboard.putNumber("Wrist", "Wrist Limit Angle", Math.abs(Robot.arm.getAngle()) >= armLimiterLowerBound && Robot.arm.getAngle() <= armLimiterUpperBound ? LUTs.wristLimits[(int)Math.ceil(Robot.arm.getAngle()) - armLimiterLowerBound] : 0);
		} catch(Exception e) {
			
		}
		
		// For TripleThreat Testbed
		//Shuffleboard.putNumber("Triple Threat", "Wrist Absolute Angle", getAbsoluteAngle());
		//Shuffleboard.putNumber("Triple Threat", "Wrist Relative Angle", getRelativeAngle());
		//Shuffleboard.putNumber("Triple Threat", "Wrist Setpoint", getPIDController().getSetpoint());
		//Shuffleboard.putNumber("Triple Threat", "Wrist Avg. Voltage", wP.getAverageVoltage());
		//Shuffleboard.putNumber("Triple Threat", "Wrist Lower Limit", angleLowerLimit);
		//Shuffleboard.putNumber("Triple Threat", "Wrist Upper Limit", angleUpperLimit);
		//Shuffleboard.putNumber("Triple Threat", "Wrist Pot Test", wristPot.get());
		
		// Use SmartDashboard to put only the important stuff for drivers;
		SmartDashboard.putNumber("Wrist Angle", getRelativeAngle());
		SmartDashboard.putNumber("Wrist Pot Voltage", wP.getAverageVoltage());
		SmartDashboard.putBoolean("Wrist PID Enabled", getPIDController().isEnabled());
	}
	
	
	@Override
	protected void initDefaultCommand() {
		// TODO Auto-generated method stub
		setDefaultCommand(new UpdateWristSetpoint());
	}
}
