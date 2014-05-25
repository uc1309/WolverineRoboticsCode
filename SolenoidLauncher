/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates.subsystems;

import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Relay;

/**
 *
 * @author Cobalt
 */
public class SolenoidLauncher extends Subsystem {
    // Put methods for controlling this subsystem
    // here. Call these from Commands.
    DigitalInput pressureSensor = new DigitalInput(13);
    Relay compressorRelay = new Relay(14);
    
    Solenoid solenoid1 = new Solenoid(11);
    Solenoid solenoid2 = new Solenoid(12);
    

    public void initDefaultCommand() {
        // Set the default command for a subsystem here.
        //setDefaultCommand(new MySpecialCommand());
    }
    
    public void solenoidOn() {
        solenoid1.set(true);
        solenoid2.set(false);
    }
    
    public void solenoidOff() {
        solenoid1.set(false);
        solenoid2.set(true);
    }
}
