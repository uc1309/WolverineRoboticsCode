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
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.templates.RobotMap;

/**
 *
 * @author Justin
 */
public class AutonomousCommand extends CommandBase {

    Timer autonomousTimer = new Timer();
    RobotDrive drive = RobotMap.drive;
    RobotDrive drive2 = RobotMap.drive2;
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
    private final boolean AUTO = true;
    private boolean hasFired = false;

    public AutonomousCommand() {
        // Use requires() here to declare subsystem dependencies
        requires(vision);
        requires(launcher);
        //requires(driveTrain);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
        //Relay ledSpike = new Relay(RobotMap.visionLEDRelay);
        //ledSpike.setDirection(Relay.Direction.kBoth);
        //ledSpike.set(Relay.Value.kForward);
        vision.visionInit();
        setTimeout(10.0);
        autonomousTimer.reset();
        autonomousTimer.start();

        front_rightE.setDistancePerPulse(distPerPulse);
        front_leftE.setDistancePerPulse(distPerPulse);
        rear_rightE.setDistancePerPulse(distPerPulse);
        rear_leftE.setDistancePerPulse(distPerPulse);
        //System.out.println("Hello World!");

        //Make sure encoders are cleared of previous runs
        front_rightE.reset();
        front_leftE.reset();
        rear_rightE.reset();
        rear_leftE.reset();

        // start the encoders counting
        front_leftE.start();
        front_rightE.start();
        rear_leftE.start();
        rear_rightE.start();
        
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
            SmartDashboard.putNumber("Autonomous Timer", autonomousTimer.get());

            double distanceToGoal = vision.getDistanceToGoal();
            double angleToGoal = vision.getAngleToGoal();
            double deltaDistance = distanceToGoal - desiredDistance;
            SmartDashboard.putNumber("Distance to goal", distanceToGoal);

            SmartDashboard.putBoolean("Hot Goal", vision.isHotGoal());
            if (distanceToGoal > 0.0) {
                if (distanceToGoal <= desiredDistance) {
                    if (autonomousTimer.get() > 5.0
                            || vision.isHotGoal()) {
                        if (LIVEMODE) {
                            launcher.autonomousLaunch();
                            hasFired = true;
                            } else {
                            System.out.println("LIVEMODE = False: Fired!");
                        }
                    }
                } else if (deltaDistance < slowDownDistance) {
                    double scale = deltaDistance / slowDownDistance; // between -1 and 1
                    double range = maxForwardSpeed - minForwardSpeed;
                    double desiredSpeed = (minForwardSpeed + range) * scale;
                    drive.mecanumDrive_Cartesian(0, -desiredSpeed, 0, 0);
                    drive2.mecanumDrive_Cartesian(0, -desiredSpeed, 0, 0);
                    SmartDashboard.putNumber("Slowing towards position", desiredSpeed);
                } else {
                    double sign = deltaDistance > 0 ? 1 : -1;
                    double angleSign = 0;
                    if (Math.abs(angleToGoal) > maxAngleToGoal) {
                        // Turn towards the goal so it stays in view
                        angleSign = angleToGoal > 0 ? 1 : -1;
                    }

                    if (STRAFE_MODE) {
                        drive.mecanumDrive_Cartesian(-angleSign * maxAngleSpeed, -sign * maxForwardSpeed, 0, 0);
                        drive2.mecanumDrive_Cartesian(-angleSign * maxAngleSpeed, -sign * maxForwardSpeed, 0, 0);
                    } else {
                        drive.mecanumDrive_Cartesian(0, -sign * maxForwardSpeed, -angleSign * maxAngleSpeed, 0);
                        drive2.mecanumDrive_Cartesian(0, -sign * maxForwardSpeed, -angleSign * maxAngleSpeed, 0);
                    }

                    //System.out.println("driving towards position: " + sign * maxForwardSpeed + " angle: " + angleSign * maxAngleSpeed);
                }
            }
        } else {
            
//           try {
//                
//                drive.mecanumDrive_Cartesian(0, -0.6, 0, 0);
//                drive2.mecanumDrive_Cartesian(0, -0.6, 0, 0);
//                wait(10);
//            } catch (InterruptedException ex) {
//                ex.printStackTrace();
//            }
            if (autonomousTimer.get() < 1.5) {
                drive.mecanumDrive_Cartesian(0, -0.5, 0, 0);
                drive2.mecanumDrive_Cartesian(0, -0.5, 0, 0);
            }
//            else if(autonomousTimer.get() >= 1.5 && autonomousTimer.get() < 3) {
//                drive.mecanumDrive_Cartesian(0, 0, 0, 0);
//                drive2.mecanumDrive_Cartesian(0, 0, 0, 0);
//                if (!launcher.hasShot) {
//                    launcher.launch();
//                }
//            }
            else {
                drive.mecanumDrive_Cartesian(0, 0, 0, 0);
                drive2.mecanumDrive_Cartesian(0, 0, 0, 0);
                launcher.setZero();
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
