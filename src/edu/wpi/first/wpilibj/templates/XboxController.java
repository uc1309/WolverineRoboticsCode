package edu.wpi.first.wpilibj.templates;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * @author patb
 */

import edu.wpi.first.wpilibj.Joystick;

public class XboxController {
    // Purpose: implements an xbox controller based on a multi button/axis
    //          joystick, and tracks the state of various stick axis and 
    //          named controller buttons
    
    
    // Constructor
    // Input: a joystick instance that is our xbox controller
    public XboxController(Joystick j){
        // initialize the internal variables
        joy = j;
        axis1 = 0;
        axis2 = 0;
        axis3 = 0;
        axis4 = 0;
        xButton = false;
        yButton = false;
        aButton = false;
        bButton = false;
        lBumper = false;
        rBumper = false;
        rStickClick = false;
        triggerAxis = 0;
    }

    // Method: updateController
    // Purpose: to update the class status variables based on the xbox
    //          controller's buttons and stick states
    // Input: none
    // Output: none
    public void updateController(){

        //trigger axis is 3
        axis1 = joy.getRawAxis(1);
        axis2 = joy.getRawAxis(2);
        axis3 = joy.getRawAxis(4);
        axis4 = joy.getRawAxis(5);
        xButton = joy.getRawButton(3);
        yButton = joy.getRawButton(4);
        aButton = joy.getRawButton(1);
        bButton = joy.getRawButton(2);
        lBumper = joy.getRawButton(5);
        rBumper = joy.getRawButton(6);
        triggerAxis = joy.getRawAxis(3);
        rStickClick = joy.getRawButton(10);
        double dzx = 0.2; //dead zone for joystick1 x axis created by billy huang<3
        double dzy = 0.1; 
        
        if((axis1 > -dzx) && (axis1 < dzx)){
            axis1 = 0;
        }
         if((axis2 > -dzy) && (axis2 < dzy)){
            axis2 = 0;
        }
//         if((axis3 > -dzx) && (axis3 < dzx)){
//            axis3 = 0;
//        }
//          if((axis4 > -dzy) && (axis4 < dzy)){
//            axis4 = 0;
//        }
    } // end method updateControler
    
    
    // Method: getStick
    // Purpose: return the state of the requested xbox stick & axis pair
    // Input:  stick (L or R) and axis (X or Y)
    // Output: returns the stick position
    public double getStick(char stickLetter, char axisLetter){
        if(stickLetter == 'L' || stickLetter == 'l'){
            if(axisLetter == 'X' || axisLetter == 'x'){
                return axis1;
            } else if(axisLetter == 'Y' || axisLetter == 'y'){
                return -axis2;
            }
        } else if(stickLetter == 'R' || stickLetter == 'r'){
            if(axisLetter == 'X' || axisLetter == 'x'){
                return axis3;
            } else if(axisLetter == 'Y' || axisLetter == 'y'){
                return axis4;
            }
        }
            return 0;

    }
    // Method getTrigger
    // Purpose: return the position of the xbox controller trigger button
    // Input: none
    // Output:  returns the extent to which the trigger is depressed
    
    // Method getTrigger
    // Purpose: return the position of the xbox controller trigger button
    // Input: none
    // Output:  returns the extent to which the trigger is depressed
    public double getTrigger(){
        return -triggerAxis;
    }
    
    public boolean getTriggerBool() {
        if (-triggerAxis > 0.2) {
            return true; 
        }
        else {
            return false;
        }
        
    }
    // Method: getButton
    // Purppose: return the state of a xbox controller button
    // Input: which button to get state information on: A,B,X,Y,R
    //      Where A is button A, X is button X, etc. R is right stick click
    // Output: returns true if button is depressed, false otherwise
    public boolean getButton(char btnLtr){
        switch(btnLtr){
            case 'A':
                return aButton;
            case 'B':
                return bButton;
            case 'X':
                return xButton;
            case 'Y':
                return yButton;
            case 'R':
                  return rStickClick;
            default: return false;
        } // end switch btnLtr
    } // end method getButton

    // Method getBumper
    // Purpose: return the position of the xbox controller bumper button
    // Input: which bumper button to test (L or R)
    // Output:  returns true if bumper button is pressed
    public boolean getBumper(char btnLtr){
        switch(btnLtr){
            case 'L':
                return lBumper;
            case 'R':
                return rBumper;
            default: return false;
        } // end switch btnLtr
    } // end method

 
 
    // Joystick axis info: forward is -1, backwards is +1
    //                     left is +1, right is -1
    private Joystick joy;
    private double axis1;
    private double axis2;
    private double axis3;
    private double axis4;
    private double triggerAxis;
    private boolean xButton;
    private boolean yButton;
    private boolean aButton;
    private boolean bButton;
    private boolean lBumper;
    private boolean rBumper;
    private boolean rStickClick;


}