/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates.subsystems;

import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.templates.RobotMap;
/**
 *
 * @author Robot
 */
public class PickupMech extends Subsystem {
    // Put methods for controlling this subsystem
    // here. Call these from Commands.
    
    Relay PickupL = new Relay(RobotMap.pickupMechLRelay);
    Relay PickupR = new Relay(RobotMap.pickupMechRRelay);
    Relay LowerPick = new Relay(RobotMap.lowerPick);
    
    public void initDefaultCommand() {
        // Set the default command for a subsystem here.
        //setDefaultCommand(new MySpecialCommand());
    }
    
    public void startPickup(){
        PickupL.set(Relay.Value.kForward);
        PickupR.set(Relay.Value.kForward);
    }
    
    public void startDrop() {
        PickupL.set(Relay.Value.kReverse);
        PickupR.set(Relay.Value.kReverse);
    }
    
    public void lowerPick() {
        LowerPick.set(Relay.Value.kForward);
    }
    
    public void raisePick() {
        LowerPick.set(Relay.Value.kReverse);
    }
    
    public void setZero(){
        PickupL.set(Relay.Value.kOff);
        PickupR.set(Relay.Value.kOff);
        LowerPick.set(Relay.Value.kOff);
    }
}
