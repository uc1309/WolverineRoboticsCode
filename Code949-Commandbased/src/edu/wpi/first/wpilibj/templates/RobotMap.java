package edu.wpi.first.wpilibj.templates;
import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.Encoder;
/**
 * The RobotMap is a mapping from the ports sensors and actuators are wired into
 * to a variable name. This provides flexibility changing wiring, makes checking
 * the wiring easier and significantly reduces the number of magic numbers
 * floating around.
 */
public class RobotMap {
    
    public static final boolean secondDriver = false;
    public static final int front_right = 2;
    public static final int front_left = 8;
    public static final int rear_right = 3;
    public static final int rear_left = 5;
    public static final int front_right2 = 1; //2nd motors are the small sim motors
    public static int front_left2 = 7;
    public static final int rear_right2 = 4;
    public static final int rear_left2 = 6;
    public static final int launcher = 9;
    public static final int launcher2 = 10;
    
    public static AnalogChannel shooterCh = new AnalogChannel(5);
    public static AnalogChannel sonarCh = new AnalogChannel(1);
    public static AnalogChannel sonarCh2 = new AnalogChannel(3);
    public static Encoder frontRightE = new Encoder(1,2,false);
    public static Encoder frontLeftE = new Encoder(7,8,true);
    public static Encoder rearRightE = new Encoder(3,4,false);
    public static Encoder rearLeftE = new Encoder(5,6,true);
    public static final int visionLEDRelay = 5;
    public static final int pickupMechLRelay = 1;
    public static final int pickupMechRRelay = 2;
}
