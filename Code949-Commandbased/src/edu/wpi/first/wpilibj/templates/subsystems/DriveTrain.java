/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates.subsystems;

import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.templates.RobotMap;
import edu.wpi.first.wpilibj.templates.commands.DriveWithXboxController;
import edu.wpi.first.wpilibj.Victor; // small victor motor controllers
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.RobotDrive;

/**
 *
 * @author Robot
 */
public class DriveTrain extends Subsystem {
    Victor front_right = new Victor(RobotMap.front_right);
    Victor front_left = new Victor(RobotMap.front_left);
    Victor rear_right = new Victor(RobotMap.rear_right);
    Victor rear_left = new Victor(RobotMap.rear_left); 
    Victor front_right2 = new Victor(RobotMap.front_right2);
    Victor front_left2 = new Victor(RobotMap.front_left2);
    Victor rear_right2 = new Victor(RobotMap.rear_right2);
    Victor rear_left2 = new Victor(RobotMap.rear_left2);
    RobotDrive drive = new RobotDrive(front_left, rear_left, front_right, rear_right);
    RobotDrive drive2 = new RobotDrive(front_left2, rear_left2, front_right2, rear_right2);
    
    public void initDefaultCommand() {
        // Set the default command for a subsystem here.
        setDefaultCommand(new DriveWithXboxController());
    }
    public void tankDrive(double leftSpeed, double rightSpeed) {
        front_right.set(rightSpeed);
        front_left.set(leftSpeed);
        rear_right.set(rightSpeed);
        rear_left.set(leftSpeed);
    }
    
    public void mechanumDrive(double xSpeed, double ySpeed, double rotation) {
        drive.setSafetyEnabled(false);
        drive.setSafetyEnabled(false);
        drive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
        drive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
        drive2.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
        drive2.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
        
        drive.mecanumDrive_Cartesian(xSpeed, ySpeed, rotation, 0);
        drive2.mecanumDrive_Cartesian(xSpeed, ySpeed, rotation, 0);
    }
    
    public void kiddieDrive(double xSpeed, double ySpeed, double rotation) {
        xSpeed *= 0.3;
        ySpeed *= 0.3;
        rotation *= 0.9;
        drive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, true);
        drive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);
        drive.mecanumDrive_Cartesian(xSpeed, ySpeed, rotation, 0);
    }
    
    public void setZero() {
        front_right.set(0.0);
        front_left.set(0.0);
        rear_right.set(0.0);
        rear_left.set(0.0);
        drive.mecanumDrive_Cartesian(0, 0, 0, 0);
    }
}
