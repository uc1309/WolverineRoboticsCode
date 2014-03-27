/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates.subsystems;

import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.templates.RobotMap;
import edu.wpi.first.wpilibj.templates.commands.*;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.PIDController;
import java.util.Timer;

/**
 *
 * @author Robot
 */
public class Launcher extends Subsystem {
    // Put methods for controlling this subsystem
    // here. Call these from Commands.
    private double targetPosValue = 255.0;
    // command magnitude to move towards target
    private double cmdMag = 0.15;
    private double tolerance = 5.0;
    public boolean hasShot = false;
    
    Victor launcher = new Victor(RobotMap.launcher);
    Victor launcher2 = new Victor(RobotMap.launcher2);
    private AnalogChannel shooterAC = RobotMap.shooterCh;
    private AnalogChannel sonarAC = RobotMap.sonarCh;
    private AnalogChannel sonarAC2 = RobotMap.sonarCh2;
    
    PIDController shooterControl = new PIDController(0.005, 0.1, 0.0, shooterAC, launcher2);
    
    public void initDefaultCommand() {
        // Set the default command for a subsystem here.
        setDefaultCommand(new LauncherSetZero());
    }
    
    public void launch() {
        launcher.set(1.0);
        launcher2.set(1.0);
    }
    
    public void reset() {
        launcher.set(-0.5);
        launcher2.set(-0.5);
        hasShot = false;
    }
    
    public void getVoltage() {
        String chan5 = "" + shooterAC.getVoltage();
        chan5 = chan5.substring(0,6);
        String chan3 = "" + sonarAC2.getVoltage();
        chan3 = chan3.substring(0,6);
        String chan1 = "" + sonarAC.getVoltage();
        chan1 = chan1.substring(0,6);
        System.out.print("Channel 1:" + chan1 + " ");
        System.out.print("Channel 3:" + chan3 + " ");
        System.out.println("Channel 5:" + chan5 + " ");
    }
    public void holdPosition() {
        // direction to go to reach target
        double direction;
        double offset = getOffset();
        if (offset >= tolerance) {
            direction = -1.0;
        } else  if (offset <= -tolerance) {
            direction = 1.0;
        } else {
            direction = -offset/tolerance;
        }
        
        double motorCmd = cmdMag * direction;
        launcher.set(motorCmd);
    }
    
    public void holdPosPID() {
        double wantedV = 1.80;
                wantedV *= 192.307692;
        if (!shooterControl.isEnable()) {
            shooterControl.enable(); 
            shooterControl.setSetpoint(wantedV);
        }
        //shooterControl.disable();
        String shooterReading = "" + shooterAC.getVoltage();
        shooterReading = shooterReading.substring(0,6);
        System.out.print("Readings: " + shooterReading + " ");
        System.out.print("Setpoint: " + shooterControl.getSetpoint() + " ");
        System.out.println("Error: " + shooterControl.getError());
    }
    
    public void disablePID() {
        shooterControl.reset();
        shooterControl.disable();
    }
    
    private double getOffset() {
        double pos = 200;//currentPos.getAverageValue();
        double offset = pos - targetPosValue;
        return offset;
    }
    
    public void setZero() {
        launcher.set(0.0);
        launcher2.set(0.0);
    }
}
