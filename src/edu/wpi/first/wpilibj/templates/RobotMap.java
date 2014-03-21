package edu.wpi.first.wpilibj.templates;
import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Victor;

/**
 * The RobotMap is a mapping from the ports sensors and actuators are wired into
 * to a variable name. This provides flexibility changing wiring, makes checking
 * the wiring easier and significantly reduces the number of magic numbers
 * floating around.
 */
public class RobotMap {
    
    public static final boolean secondDriver = true;
    public static final int front_right = 2;
    public static final int front_left = 8;
    public static final int rear_right = 3;
    public static final int rear_left = 5;
    public static final int front_right2 = 1; //2nd motors are the small sim motors
    public static final int front_left2 = 7;
    public static final int rear_right2 = 4;
    public static final int rear_left2 = 6;
    public static final int launcher = 9;
    public static final int launcher2 = 10;
    
    public static AnalogChannel shooterCh = new AnalogChannel(5);
    public static AnalogChannel sonarCh = new AnalogChannel(1);
    public static AnalogChannel sonarCh2 = new AnalogChannel(3);
    public static Encoder front_rightE = new Encoder(1,2,false);
    public static Encoder front_leftE = new Encoder(3,4,true);
    public static Encoder rear_rightE = new Encoder(5,6,false);
    public static Encoder rear_leftE = new Encoder(7,8, true);
    public static final int visionLEDRelay = 4;
    public static final int pickupMechLRelay = 1;
    public static final int pickupMechRRelay = 2;
    public static final int lowerPick = 3;
    
    public static Victor front_rightController = new Victor(RobotMap.front_right);
    public static Victor front_leftController = new Victor(RobotMap.front_left);
    public static Victor rear_rightController = new Victor(RobotMap.rear_right);
    public static Victor rear_leftController = new Victor(RobotMap.rear_left); 
    public static Victor front_right2Controller = new Victor(RobotMap.front_right2);
    public static Victor front_left2Controller = new Victor(RobotMap.front_left2);
    public static Victor rear_right2Controller = new Victor(RobotMap.rear_right2);
    public static Victor rear_left2Controller = new Victor(RobotMap.rear_left2);
    
    public static RobotDrive drive = new RobotDrive(front_leftController, rear_leftController, front_rightController, rear_rightController);
    public static RobotDrive drive2 = new RobotDrive(front_left2Controller, rear_left2Controller, front_right2Controller, rear_right2Controller);
}
