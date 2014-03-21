/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates.commands;

/**
 *
 * @author Robot
 */
public class TeleLaunch extends CommandBase {
    
    public TeleLaunch() {
        requires(launcher);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
        launcher.autoReset();
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        if(launcher.hasShot) {
            launcher.hasShot = false;
            return true;
        }
        return false;
    }

    // Called once after isFinished returns true
    protected void end() {
        launcher.setZero();
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
        launcher.setZero();
    }
}
