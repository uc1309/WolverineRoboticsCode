/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates.commands;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.templates.RobotMap;

/**
 *
 * @author Justin
 */
public class AutonomousCommand extends CommandBase {

    Command autoGroup = new AutonomousGroup();
    Timer autonomousTimer = new Timer();
    RobotDrive drive = RobotMap.drive;
    RobotDrive drive2 = RobotMap.drive2;
    
    Victor front_right = RobotMap.front_rightController;
    Victor front_right2 = RobotMap.front_right2Controller;
    Victor front_left = RobotMap.front_leftController;
    Victor front_left2 = RobotMap.front_left2Controller;
    Victor rear_right = RobotMap.rear_rightController;
    Victor rear_right2 = RobotMap.rear_right2Controller;
    Victor rear_left = RobotMap.rear_leftController;
    Victor rear_left2 = RobotMap.rear_right2Controller;
    
    Encoder front_rightE = RobotMap.front_rightE;
    Encoder front_leftE = RobotMap.front_leftE;
    Encoder rear_rightE = RobotMap.rear_rightE;
    Encoder rear_leftE = RobotMap.rear_leftE;
    
    private final double wheelCircumference = 18.84; // in inches (dist moved per revolution)
    private final double pulsePerRotation = 250.0;    // how many pulses per revolution
    private final double distPerPulse = (wheelCircumference / pulsePerRotation);
    private final double slowDownDistance = 30;
    private final double desiredDistance = 100;
    private final double maxForwardSpeed = 0.5;
    private final double minForwardSpeed = 0.1;
    private final double maxAngleSpeed = 0.2;
    private final double maxAngleToGoal = 5;
    private final boolean STRAFE_MODE = true;
    private final boolean LIVEMODE = true;
    private final boolean AUTO = false;
    private boolean hasFired = false;

    public AutonomousCommand() {
        // Use requires() here to declare subsystem dependencies
        requires(vision);
        requires(launcher);
        requires(driveTrain);
        requires(pickup);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
        //Relay ledSpike = new Relay(RobotMap.visionLEDRelay);
        //ledSpike.setDirection(Relay.Direction.kBoth);
        //ledSpike.set(Relay.Value.kForward);
        if(!AUTO)
            autoGroup.start();
        
        vision.visionInit();
        setTimeout(10.0);
        autonomousTimer.reset();
        autonomousTimer.start();
        autonomousTimer.reset();

        front_rightE.setDistancePerPulse(distPerPulse);
        front_leftE.setDistancePerPulse(distPerPulse);
        rear_rightE.setDistancePerPulse(distPerPulse);
        rear_leftE.setDistancePerPulse(distPerPulse);

        // start the encoders counting
        front_leftE.start();
        front_rightE.start();
        rear_leftE.start();
        rear_rightE.start();
        
        //Make sure encoders are cleared of previous runs
        front_rightE.reset();
        front_leftE.reset();
        rear_rightE.reset();
        rear_leftE.reset();
        
        //orient the motors
        drive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
        drive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
        drive2.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
        drive2.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
        
        drive.setSafetyEnabled(false);
        drive2.setSafetyEnabled(false);
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
        if (AUTO) {
            //using PID values (still untested)
            if (front_rightE.getDistance() < -120) {
                front_right.set(-0.4);
                front_right2.set(-0.4);
            }
            else {
                front_right.set(0);
                front_right2.set(0);
            }
            
            if (front_leftE.getDistance() < -120) {
                front_left.set(-0.4);
                front_left2.set(-0.4);
            }
            else {
                front_left.set(0);
                front_left2.set(0);
            }
            if (rear_rightE.getDistance() < -120) {
                rear_right.set(-0.4);
                rear_right2.set(-0.4);
            }
            else {
                rear_right.set(0);
                rear_right2.set(0);
            }
            
            if (rear_leftE.getDistance() < -120) {
                rear_left.set(-0.4);
                rear_left2.set(-0.4);
            }
            else {
                rear_left.set(0);
                rear_left2.set(0);
            }
            
        }
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return isTimedOut() || hasFired;
    }

    // Called once after isFinished returns true
    protected void end() {
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    }
}
