package org.usfirst.frc.team4201.robot.commands.autonomous.routines;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.CommandGroup;

import org.usfirst.frc.team4201.robot.commands.*;
import org.usfirst.frc.team4201.robot.commands.autonomous.*;

public class LeftAutoDoubleScale extends CommandGroup{
	
	public LeftAutoDoubleScale() {
		addSequential(new SetIntakePistonsClose());
		addSequential(new SetIntakePressureHigh());
		
		if(DriverStation.getInstance().getGameSpecificMessage().charAt(1) == 'L') {		
			addSequential(new AutoPathFinderInvertedToScaleClose("leftStartToLeftScale", true));
			
			addSequential(new AutoSetWristScaleScoring(120, true));
			addSequential(new Delay(0.2));
			addSequential(new AutoShootCube(-1, 0.5));
			addParallel(new AutoSetIntakeMotorOutputsContinouous(1));
			addSequential(new AutoIntakeCube("scaleToEdgeCube", false, 1.75));
			addSequential(new AutoGrabCube());
			addSequential(new AutoSetIntakeMotorOutputsContinouous(0));
				
			addSequential(new AutoPathFinderInvertedToScaleCloseHigher("edgeCubeToScaleShoot", false, 4));
			addSequential(new AutoSetWristScaleScoring(135, true));
			addSequential(new AutoShootCube(-1, 0.5));
		} else {
			addSequential(new AutoPathFinderInvertedToScaleFar("leftStartToRightScale", true));
		
			addSequential(new AutoSetWristScaleScoring(120, true));
			addSequential(new Delay(0.2));
			addSequential(new AutoShootCube(-1, 0.5));
			addParallel(new AutoSetIntakeMotorOutputsContinouous(1));
			addSequential(new AutoIntakeCube("scaleToEdgeCubeFar", false, 1));
			addSequential(new AutoGrabCube());
			addSequential(new AutoSetIntakeMotorOutputsContinouous(0));
				
			addSequential(new AutoPathFinderInvertedToScaleCloseHigher("edgeCubeFarToScale", false, 4));
			addSequential(new AutoSetWristScaleScoring(135, true));
			addSequential(new AutoShootCube(-1, 0.5));
		}
		/*
		addSequential(new AutoSetIntakingPosition());
		addParallel(new AutoSetIntakeMotorOutputsContinouous(1));
		addSequential(new PathFinderRead("scaleToEdgeCube", false, 4));
		addSequential(new AutoGrabCube());
		addSequential(new AutoSetIntakeMotorOutputsContinouous(0));
		
		addSequential(new AutoPathFinderInvertedToScaleClose("edgeCubeToScale", false, 4));
		addSequential(new AutoShootCube(-0.75, 0.75));
		//*/
		
		addSequential(new AutoReleaseWristSetpoint());
	}
}
