
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.DigitalIOButton;
import edu.wpi.first.wpilibj.Joystick; // joystick i/o; needed by gamepad
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.templates.RobotMap;
import edu.wpi.first.wpilibj.templates.commands.*;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the commands and command groups that allow control of the robot.
 */
public class OI {
    
    private final boolean secondDriver = RobotMap.secondDriver;
    private final double ACCEL_LIMIT = 0.2;
    private final double turnRatio = 0.1;
    private final double deadband = 0.02;
    
    Joystick joyStick1 = new Joystick(1);    // define our joystick from port 1
    Joystick joyStick2 = new Joystick(2);
    XboxController xBox1 = new XboxController(joyStick1); // define our xbox controller
    XboxController xBox2 = new XboxController(joyStick2);
    
    public OI() {
        if (secondDriver) {
            JoystickButton A = new JoystickButton(joyStick2, 1); //A = 1, B = 2, X = 3, Y = 4
            A.whileHeld(new Pickup());
            JoystickButton X = new JoystickButton(joyStick2, 3);
            X.whileHeld(new HoldLauncherPosition());
            JoystickButton R1 = new JoystickButton(joyStick2, 6);  //6 = right bumper
            R1.whileHeld(new AutoLaunch());
            JoystickButton Y = new JoystickButton(joyStick2, 4);
            Y.whileHeld(new ResetLauncher());
            JoystickButton B = new JoystickButton(joyStick2, 2);
            B.whileHeld(new Drop());
        }
        else {
            JoystickButton A = new JoystickButton(joyStick1, 1); //A = 1, B = 2, X = 3, Y = 4
            A.whileHeld(new Pickup());
            JoystickButton X = new JoystickButton(joyStick1, 3);
            X.whileHeld(new HoldLauncherPosition());
            JoystickButton R1 = new JoystickButton(joyStick1, 6);  //6 = right bumper
            R1.whenPressed(new AutoLaunch());
            JoystickButton L1 = new JoystickButton(joyStick1, 5);
            L1.whileHeld(new ManualLaunch());
            JoystickButton Y = new JoystickButton(joyStick1, 4);
            Y.whileHeld(new ResetLauncher());
            JoystickButton B = new JoystickButton(joyStick1, 2);
            B.whileHeld(new Drop());
        }        
    }
    //Github mercurial
    public double getLeftSpeed() {
        xBox1.updateController(); // update the xbox controller state info
        
        // Direction correction: left = counterclockwise, right = clockwise
        final double LEFTDIR = 1;
        
        double robotSpeed = 0.0;

        // Determine requested speed and rotation from the x and y joystick axis
        double requestedSpeed = xBox1.getStick('L', 'Y'); // speed to set; LEFT stick, Y axis

        // compute the adjusted requested speed to control accelleration
        double spd_change = (requestedSpeed - robotSpeed);
        if (Math.abs(spd_change) > ACCEL_LIMIT) {
            // if we are asking to change speed by an amount more than the
            // fixed limit, we need to reduce the amount of speed change we
            // actually apply this iteration
            if (spd_change > 0) {
                // robot is moving forward
                spd_change = +ACCEL_LIMIT; // no faster than what we limited
            } // end if moving forwards
            else {
                // robot is moving backwards
                spd_change = -ACCEL_LIMIT;
            } // end if moving backwards
        } // end if we need to throttle the speed change

        double adjSpeedReq = robotSpeed + spd_change; // compute the speed we will set
        robotSpeed = adjSpeedReq;                     // and update the current speed to that new speed

        double leftSpeed = adjSpeedReq * LEFTDIR;
        
        double joyFctr = -xBox1.getStick('L', 'Y');

        joyFctr = applyDeadband(joyFctr);
        leftSpeed *= joyFctr;
        return leftSpeed;
    }
    
    public double getRightSpeed() {
        xBox1.updateController(); // update the xbox controller state info

        final double RIGHTDIR = 1;
        double robotSpeed = 0.0;
        
        // Determine requested speed and rotation from the x and y joystick axis
        double requestedSpeed = xBox1.getStick('R', 'Y'); // speed to set; LEFT stick, Y axis

        // compute the adjusted requested speed to control accelleration
        double spd_change = (requestedSpeed - robotSpeed);
        if (Math.abs(spd_change) > ACCEL_LIMIT) {
            // if we are asking to change speed by an amount more than the
            // fixed limit, we need to reduce the amount of speed change we
            // actually apply this iteration
            if (spd_change > 0) {
                // robot is moving forward
                spd_change = +ACCEL_LIMIT; // no faster than what we limited
            } // end if moving forwards
            else {
                // robot is moving backwards
                spd_change = -ACCEL_LIMIT;
            } // end if moving backwards
        } // end if we need to throttle the speed change

        double adjSpeedReq = robotSpeed + spd_change; // compute the speed we will set
        robotSpeed = adjSpeedReq;                     // and update the current speed to that new speed

        double rightSpeed = adjSpeedReq * RIGHTDIR;
        
        double joyFctr = -xBox1.getStick('R', 'Y');

        joyFctr = applyDeadband(joyFctr);
        rightSpeed *= joyFctr;
        return rightSpeed;
    }
    
    public double getRotation() {
        xBox1.updateController(); // update the xbox controller state info
        // Determine requested speed and rotation from the x and y joystick axis
        double requestedSpeed = -xBox1.getStick('R', 'X'); // speed to set; LEFT stick, Y axis
        
        double robotSpeed = 0.0;
        // compute the adjusted requested speed to control accelleration
        double spd_change = (requestedSpeed - robotSpeed);
        if (Math.abs(spd_change) > ACCEL_LIMIT) {
            // if we are asking to change speed by an amount more than the
            // fixed limit, we need to reduce the amount of speed change we
            // actually apply this iteration
            if (spd_change > 0) {
                // robot is moving forward
                spd_change = +ACCEL_LIMIT; // no faster than what we limited
            } // end if moving forwards
            else {
                // robot is moving backwards
                spd_change = -ACCEL_LIMIT;
            } // end if moving backwards
        } // end if we need to throttle the speed change

        double joyFctr = -xBox1.getStick('R', 'X');

        joyFctr = applyDeadband(joyFctr);
        double adjSpeedReq = robotSpeed + spd_change;
        adjSpeedReq *= joyFctr;
        return adjSpeedReq;
    }
    
    public double getXSpeed() {
        xBox1.updateController(); // update the xbox controller state info
        
        // Determine requested speed and rotation from the x and y joystick axis
        double reqSpeed = xBox1.getStick('L', 'X'); // speed to set; LEFT stick, X axis
        
        double robotSpeed = 0.0;
        // compute the adjusted requested speed to control accelleration
        double spd_change = (reqSpeed - robotSpeed);
        if (Math.abs(spd_change) > ACCEL_LIMIT) {
            // if we are asking to change speed by an amount more than the
            // fixed limit, we need to reduce the amount of speed change we
            // actually apply this iteration
            if (spd_change > 0) {
                // robot is moving forward
                spd_change = +ACCEL_LIMIT; // no faster than what we limited
            } // end if moving forwards
            else {
                // robot is moving backwards
                spd_change = -ACCEL_LIMIT;
            } // end if moving backwards
        } // end if we need to throttle the speed change
        
        double joyFctr = -xBox1.getStick('L', 'X');

        joyFctr = applyDeadband(joyFctr);
        double adjSpeedReq = robotSpeed + spd_change;
        adjSpeedReq *= joyFctr;
        return adjSpeedReq;
    }
    
    public double getRawYSpeed() {
        xBox1.updateController();
        double ySpeed = xBox1.getStick('L', 'Y');
        return applyDeadband(ySpeed);
    }
    
    public double getRawXSpeed() {
        xBox1.updateController();
        double xSpeed = -xBox1.getStick('L', 'X');
        return applyDeadband(xSpeed);
    }
    
    public double getRawRotation() {
        xBox1.updateController();
        double rSpeed = -xBox1.getStick('R', 'X');
        return applyDeadband(rSpeed);
    }
    
    public double cameraServoY() {
        if (secondDriver) {
            xBox2.updateController();
            return (-xBox2.getStick('R', 'Y') / 2.0) + 0.509;
        }
        else {
            return 0.0;
        }
    }
    
    public double cameraServoX() {
        if (secondDriver) {
            xBox2.updateController();
            return (xBox2.getStick('R', 'X') / 2.0) + 0.509;
        }
        else {
            return 0.0;
        }
    }
    
    private double applyDeadband(double val) {
        if (Math.abs(val) < deadband) {
            return 0.0;
        }
        return val;
    }
}

