/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates.commands;
/**
 *
 * @author Justin
 */
public class DriveWithXboxController extends CommandBase {
    
    public DriveWithXboxController() {
        super("DriveWithXboxController");
        requires(driveTrain);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
        driveTrain.mechanumDrive(oi.getRawXSpeed(), oi.getRawYSpeed(), oi.getRawRotation());
        //driveTrain.tankDrive(oi.getLeftSpeed(), oi.getRightSpeed());
        //driveTrain.kiddieDrive(oi.getXSpeed(), oi.getLeftSpeed(), oi.getRotation());
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return false;
    }

    // Called once after isFinished returns true
    protected void end() {
        driveTrain.setZero();
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
        driveTrain.setZero();
    }
}
