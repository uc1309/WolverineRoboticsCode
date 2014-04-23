/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates.commands;

/**
 *
 * @author Justin
 */
public class AutonomousDrive extends CommandBase {
    
    public AutonomousDrive() {
        // Use requires() here to declare subsystem dependencies
        requires(driveTrain);
        setTimeout(3.0);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
        driveTrain.setZero();
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
        driveTrain.mechanumDrive(0, -0.25, 0);
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return isTimedOut();
    }

    // Called once after isFinished returns true
    protected void end() {
        driveTrain.setZero();
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    }
}
