/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates.subsystems;

import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.templates.RobotMap;
import edu.wpi.first.wpilibj.templates.commands.DriveWithXboxController;
import edu.wpi.first.wpilibj.RobotDrive;

/**
 *
 * @author Justin
 */
public class DriveTrain extends Subsystem {
    RobotDrive drive = RobotMap.drive;
    RobotDrive drive2 = RobotMap.drive2;
    
    public void initDefaultCommand() {
        // The default command for the subsystem is to drive with the XboxController
        setDefaultCommand(new DriveWithXboxController());
    }
    
    public void mechanumDrive(double xSpeed, double ySpeed, double rotation) {
        drive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
        drive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
        drive2.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
        drive2.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
        
        drive.mecanumDrive_Cartesian(xSpeed, ySpeed, rotation, 0);
        drive2.mecanumDrive_Cartesian(xSpeed, ySpeed, rotation, 0);
    }
    
    public void setZero() {
        drive.mecanumDrive_Cartesian(0, 0, 0, 0);
        drive2.mecanumDrive_Cartesian(0, 0, 0, 0);
    }
}
