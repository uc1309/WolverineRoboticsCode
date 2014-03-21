/*
* Yay! ...Stupid autonomous. So hard.
*/
package edu.wpi.first.wpilibj.templates.commands;

import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.templates.commands.*;
import edu.wpi.first.wpilibj.templates.RobotMap;

public class Auto extends CommandBase {
    RobotDrive chassisDrive = RobotMap.drive;
    RobotDrive chassisDrive2 = RobotMap.drive2;
    
    Timer autonomousTimer;
    NetworkTable mainTable;
    ITable netTable;
    
    double desiredDistance;
    double desiredDistanceEpsilon;
    double slowDownDistance;
    double maxAngleToGoal;
    double maxForwardSpeed;
    double minSpeed;
    double maxAngleSpeed;
    
    Command pickupMech;
    Command shooter;
    
            
    boolean LIVE_MODE = true;
    boolean SHOOT_MODE = true;
    boolean STRAFE_MODE = false;
    boolean AT_FIRING_DISTANCE = false;
    double firingDistanceTime = -1;
    boolean FIRED = false;
    boolean auto = true;
    
    public Auto() {
        // Use requires() here to declare subsystem dependencies
        // eg. requires(chassis);
	
        mainTable = NetworkTable.getTable("SmartDashboard");
        netTable = mainTable.getSubTable("Autonomous");
        autonomousTimer = new Timer();
        netTable.putNumber("DesiredDistance", 95); // inches
        netTable.putNumber("DesiredDistanceEpsilon", 6); // inches
        netTable.putNumber("SlowDownDistance", 30);
        netTable.putNumber("MaxAngle", 5);
        netTable.putNumber("MaxForwardSpeed", 0.4);
        netTable.putNumber("MinForwardSpeed", 0.1);
        netTable.putNumber("MaxAngleSpeed", 0.2);
        netTable.putBoolean("LiveMode", true);
        netTable.putBoolean("ShootMode", true);
        netTable.putBoolean("StrafeMode", false);
    }
    
    // Called just before this Command runs the first time
    protected void initialize() {
        System.out.println("Initialize autonomous mode");
        setTimeout(10.0);
        autonomousTimer.reset();
        autonomousTimer.start();
        
        pickupMech = new Pickup();
        shooter = new Shoot();
        FIRED = false;
    }
    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
        desiredDistance = netTable.getNumber("DesiredDistance"); // inches
        desiredDistanceEpsilon = netTable.getNumber("DesiredDistanceEpsilon"); // inches
        slowDownDistance = netTable.getNumber("SlowDownDistance");
        maxAngleToGoal = netTable.getNumber("MaxAngle");
        maxForwardSpeed = netTable.getNumber("MaxForwardSpeed");
        minSpeed = netTable.getNumber("MinForwardSpeed");
        maxAngleSpeed = netTable.getNumber("MaxAngleSpeed");
        LIVE_MODE = netTable.getBoolean("LiveMode");
        SHOOT_MODE = netTable.getBoolean("ShootMode");
        STRAFE_MODE = netTable.getBoolean("StrafeMode");
        //System.out.println("dist to goal: " + distanceToGoal);
        
        double distanceToGoal = vision.getDistanceToGoal();
        double angleToGoal = vision.getAngleToGoal();
        
        if (autonomousTimer.get() < 0.5) {
            pickupMech.start();
        }
        else {
            pickupMech.cancel();
        }

        if (distanceToGoal > 0.0 && distanceToGoal < 360.0 && auto) { // if we have a valid distance
            double deltaDistance = distanceToGoal - desiredDistance;
            // drive to goal
            if (Math.abs(deltaDistance) < desiredDistanceEpsilon) {
                // in range, FIRE!
                
                if (LIVE_MODE) {
                    chassisDrive.mecanumDrive_Cartesian(0,0,0,0);
                    chassisDrive2.mecanumDrive_Cartesian(0,0,0,0);
                }
                // do we want a delay?
                
                if (autonomousTimer.get() > 5.0 ||
                    vision.isHotGoal()) {
                    // fire
                    System.out.println("in firing position");
                    if (!AT_FIRING_DISTANCE) {
                        firingDistanceTime = autonomousTimer.get();
                        AT_FIRING_DISTANCE = true;
                    }
                    if (autonomousTimer.get() > 2.0 &&
                            firingDistanceTime > 0 &&
                            autonomousTimer.get() > firingDistanceTime + 1.0) {
                        System.out.println("fire!!!");
                        if (SHOOT_MODE)
                            shooter.start();
                        FIRED = true;
                    }
                }
                else {
                    
                }
            }
            else if (Math.abs(deltaDistance) < slowDownDistance) {
                double scale = deltaDistance / slowDownDistance; // between -1 and 1
                double range = maxForwardSpeed - minSpeed;
                double desiredSpeed = (minSpeed + range) * scale;
                if (LIVE_MODE) {
                    chassisDrive.mecanumDrive_Cartesian(0, desiredSpeed, 0, 0);
                    chassisDrive2.mecanumDrive_Cartesian(0, desiredSpeed, 0, 0);
                }
                System.out.println("slowing towards position: " + desiredSpeed);
            }
            else {
                double sign = deltaDistance > 0 ? 1 : -1;
                double angleSign = 0;
                if (Math.abs(angleToGoal) > maxAngleToGoal) {
                    // Turn towards the goal so it stays in view
                    angleSign = angleToGoal > 0 ? 1 : -1;
                }
                //chassisDrive.tankDrive(sign * maxSpeed, sign * maxSpeed);
                if (LIVE_MODE) {
                    if (STRAFE_MODE) {
                        chassisDrive.mecanumDrive_Cartesian(angleSign * maxAngleSpeed, sign * maxForwardSpeed, 0, 0);
                        chassisDrive2.mecanumDrive_Cartesian(angleSign * maxAngleSpeed, sign * maxForwardSpeed, 0, 0);
                    }
                    else {
                        chassisDrive.mecanumDrive_Cartesian(0, sign * maxForwardSpeed, angleSign * maxAngleSpeed, 0);
                        chassisDrive2.mecanumDrive_Cartesian(0, sign * maxForwardSpeed, angleSign * maxAngleSpeed, 0);
                    }
                }
                System.out.println("driving towards position: " + sign * maxForwardSpeed + " angle: " + angleSign * maxAngleSpeed);
            }
        }
        else {
            // Can't see the target, so just run blind?
            if (autonomousTimer.get() < 5.0) {
                if (LIVE_MODE) {
                    chassisDrive.mecanumDrive_Cartesian(0, maxForwardSpeed / 2, 0, 0);
                    chassisDrive2.mecanumDrive_Cartesian(0, maxForwardSpeed / 2, 0, 0);
                }
                //System.out.println("driving blind: " + maxForwardSpeed);
            }
            else if (autonomousTimer.get() < 3.0) {
                // stop
                //System.out.println("stopping blind: " + 0.0);
                if (LIVE_MODE) {
                    chassisDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
                    chassisDrive2.mecanumDrive_Cartesian(0, 0, 0, 0);
                }
            } else {
                //System.out.println("firing blind");
                if (LIVE_MODE) {
                    chassisDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
                    chassisDrive2.mecanumDrive_Cartesian(0, 0, 0, 0);
                }
                if (SHOOT_MODE)
                    shooter.start();
                FIRED = true;
            }
            
            // Can't see the target, so just turn-in-place?
            // Alternate option
            //chassisDrive.mecanumDrive_Cartesian(0, 0, maxAngleSpeed, 0);
            //chassisDrive2.mecanumDrive_Cartesian(0, 0, maxAngleSpeed, 0);
        }
    }
    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return isTimedOut() || FIRED;
    }
    // Called once after isFinished returns true
    protected void end() {
    }
    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    }
}
