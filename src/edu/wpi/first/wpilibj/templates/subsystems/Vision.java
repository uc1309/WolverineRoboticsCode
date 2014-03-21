/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates.subsystems;

import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.camera.AxisCameraException;
import edu.wpi.first.wpilibj.templates.RobotMap;
import edu.wpi.first.wpilibj.templates.commands.*;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.image.BinaryImage;
import edu.wpi.first.wpilibj.image.ColorImage;
import edu.wpi.first.wpilibj.image.CriteriaCollection;
import edu.wpi.first.wpilibj.image.NIVision;
import edu.wpi.first.wpilibj.image.NIVisionException;
import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.networktables2.type.NumberArray;
import edu.wpi.first.wpilibj.tables.TableKeyNotDefinedException;
/**
 *
 */

public class Vision extends Subsystem {
    Relay ledSpike = new Relay(RobotMap.visionLEDRelay);
    //Camera constants used for distance calculation
    final int Y_IMAGE_RES = 240;		//Y Image resolution in pixels, should be 120, 240 or 480
    final int X_IMAGE_RES = 320;
    //final double VIEW_ANGLE = 49;		//Axis M1013
    final double HORIZONTAL_VIEW_ANGLE = 47;
    //final double VIEW_ANGLE = 41.7;		//Axis 206 camera
    final double VIEW_ANGLE = 37.4;  //Axis M1011 camera
    final double PI = 3.141592653;
    //Score limits used for target identification
    final int  RECTANGULARITY_LIMIT = 40;
    final int ASPECT_RATIO_LIMIT = 55;
    //Score limits used for hot target determination
    final int TAPE_WIDTH_LIMIT = 50;
    final int  VERTICAL_SCORE_LIMIT = 50;
    final int LR_SCORE_LIMIT = 50;
    //Minimum area of particles to be considered
    final int AREA_MINIMUM = 150;
    //Maximum number of particles to process
    final int MAX_PARTICLES = 8;
    AxisCamera camera;          // the axis camera object (connected to the switch)
    CriteriaCollection cc;      // the criteria for doing the particle filter operation
    Preferences prefs = Preferences.getInstance();
    int DEBUG_LEVEL = prefs.getInt("VisionDebugLevel", 1);
    public class Rect {
        // Store some information about the rectangle
        double points[] = new double[8];
        double x[] = new double[4];
        double y[] = new double[4];
        double bbWidth;
        double bbHeight;
        double bbLeft;
        double bbRight;
        double bbTop;
        double bbBottom;
        double rectLong;
        double rectShort;
        double center_mass_x;
        double center_mass_y;
        
        Rect(NumberArray numberArray, int index) {
            for (int i = 0; i < 8; i++) {
                points[i] = numberArray.get(index * 8 + i);
            }
            for (int i = 0; i < 4; i++) {
                x[i] = numberArray.get(index * 8 + i * 2);
            }
            for (int i = 0; i < 4; i++) {
                y[i] = numberArray.get(index * 8 + i * 2 + 1);
            }
        }
    }
        
    public class Scores {
        double rectangularity;
        double aspectRatioVertical;
        double aspectRatioHorizontal;
    }
    
    public class TargetReport {
		int verticalIndex;
		int horizontalIndex;
		boolean Hot;
                boolean leftHot; // is hot target to left of vertical target
		double totalScore;
		double leftScore;
		double rightScore;
		double tapeWidthScore;
		double verticalScore;
                double distance;
                double angle;
    };
    
    TargetReport lastTarget = new TargetReport();
    
    NetworkTable netTable;
    boolean useNetTable;    
    // Put methods for controlling this subsystem
    // here. Call these from Commands.
    public void initDefaultCommand() {
        //setDefaultCommand(new GetDistanceToGoal());
	
        // Set the default command for a subsystem here.
        //setDefaultCommand(new MySpecialCommand());
    }
    
    public void visionInit() {
        netTable = NetworkTable.getTable("SmartDashboard");
        ledSpike.set(Relay.Value.kOff);
	try
	{
            if (DEBUG_LEVEL >= 3)
                System.out.println(netTable.getNumber("IMAGE_COUNT", 0.0));
            useNetTable = true;
	}
	catch (TableKeyNotDefinedException ex)
	{
            System.out.println("RoboRealm not initialized. Falling back to cRIO processing");
            useNetTable = false;
            camera = AxisCamera.getInstance();  // get an instance of the camera
            cc = new CriteriaCollection();      // create the criteria for the particle filter
            cc.addCriteria(NIVision.MeasurementType.IMAQ_MT_AREA, AREA_MINIMUM, 65535, false);
        }
    }
    
    public NumberArray getRectanglesFromNetTable() {
        final NumberArray targetArray = new NumberArray();
        netTable.retrieveValue("BFR_COORDINATES", targetArray);
        
        if (targetArray.size()>0)
	{
	}
        
        return targetArray;
    }
    public NumberArray getRectanglesFromCrio() {    
	final NumberArray targetArray = new NumberArray();
        // TODO: Populate from 2014VisionSampleProject code
        return targetArray;
    }
    public Rect[] getRectsFromArray(NumberArray rectanglesArray) {
        int numOfRects = rectanglesArray.size() / 8;
        Rect rects[] = new Rect[numOfRects];
        
        for (int i = 0; i < numOfRects; i++) {
            rects[i] = new Rect(rectanglesArray, i);
        }
        
        return rects;
    }
    public double getCenterMass(Rect rectangle, boolean getY) {
        int modifier;
        modifier = getY ? 1 : 0;
        double val = 0;
        
        for (int i = 0; i < 4; i++) {
            val += rectangle.points[i * 2 + modifier];
        }
        
        return val / 4;
    }
    
    public void populateRectangleSizes(Rect rectangle) {
        populateRectangleBounds(rectangle);
        rectangle.rectLong = getRectangleLength(rectangle, false);
        rectangle.rectShort = getRectangleLength(rectangle, true);        
        rectangle.center_mass_x = getCenterMass(rectangle, false);
        rectangle.center_mass_y = getCenterMass(rectangle, true);
        
        if (DEBUG_LEVEL >= 4) {
            System.out.print("rect values:");
            for (int i = 0; i < 8; i++) {
                System.out.print(" " + rectangle.points[i]);
            }
            System.out.println("");
            System.out.print("x values: ");
            for (int i = 0; i < 4; i++) {
                System.out.print(" " + rectangle.x[i]);
            }
            System.out.println("");
            System.out.print("y values: ");
            for (int i = 0; i < 4; i++) {
                System.out.print(" " + rectangle.y[i]);
            }
        }
    }
    
    public double getRectangleLength(Rect rectangle, boolean shortSide) {
        // assume 4 coordinates are in order
        double lengths[] = new double[4];
        double averageLength = 0.0;
        
        for (int i = 0; i < 4; i++) {
            double firstX = rectangle.x[(i) % 4];
            double firstY = rectangle.y[(i) % 4];
            double secondX = rectangle.x[(i + 1) % 4];
            double secondY = rectangle.y[(i + 1) % 4];
            lengths[i] = Math.sqrt((firstX - secondX)*(firstX - secondX) + (firstY - secondY) * (firstY - secondY));
            averageLength += lengths[i];
        }
        
        averageLength /= 4.0;
        
        if (shortSide) {
            if (lengths[0] < averageLength) {
                return (lengths[0] + lengths[2]) / 2.0;
            }
            else {
                return (lengths[1] + lengths[3]) / 2.0;
            }
        }
        else {
            if (lengths[0] > averageLength) {
                return (lengths[0] + lengths[2]) / 2.0;
            }
            else {
                return (lengths[1] + lengths[3]) / 2.0;
            }
        }
        
    }
    public void populateRectangleBounds(Rect rectangle) {
        rectangle.bbLeft = 1000;
        rectangle.bbRight = 0;
        rectangle.bbTop = 0;
        rectangle.bbBottom = 1000;
        
        for (int i = 0; i < 4; i++) {
            if (rectangle.x[i] > rectangle.bbRight)
                rectangle.bbRight = rectangle.x[i];
            if (rectangle.x[i] < rectangle.bbLeft)
                rectangle.bbLeft = rectangle.x[i];
            if (rectangle.y[i] > rectangle.bbTop)
                rectangle.bbTop = rectangle.y[i];
            if (rectangle.y[i] < rectangle.bbBottom)
                rectangle.bbBottom = rectangle.y[i];
        }
        
        rectangle.bbWidth = rectangle.bbRight - rectangle.bbLeft;
        rectangle.bbHeight = rectangle.bbTop - rectangle.bbBottom;
    }
    
    public double getRectangleSize(Rect rectangle, boolean height) {
        // We want to average the top values and bottom values (and left/right respectively)
        // This won't work if the image is *too* tilted
        double lowSum = 0;
        double highSum = 0;
        double sum = 0;
        
        int modifier = height ? 1 : 0;
        
        // cycle through the 4 points in the "rectangle"
        for (int i = 0; i < 4; i++) {
            sum += rectangle.points[i * 2 + modifier];
        }
        
        for (int i = 0; i < 4; i++) {
            double val = rectangle.points[i * 2 + modifier];
            if (val < sum / 4) {
                lowSum += val;
            }
            else {
                highSum += val;
            }
        }
        return (highSum - lowSum) / 2;
    }
    
    public double getAngleToGoal() {
        return lastTarget.angle;
    }
    public boolean isHotGoal() {
        return lastTarget.Hot;
    }
    
    public double getLateralDistanceToGoal() {
        // We might need this?
        return Math.tan(lastTarget.angle * PI / 180) * lastTarget.distance;
    }
    
    public double getDistanceToGoal() {
        if (useNetTable) {
            NumberArray rectanglesArray = getRectanglesFromNetTable();
            if (DEBUG_LEVEL >= 3)
                System.out.println("Number of points in array: " + rectanglesArray.size());
            
            TargetReport target = new TargetReport();
            if (rectanglesArray.size() > 0) {
                if (rectanglesArray.size() % 8 != 0) {
                    System.out.println("Error: Number of points in array not divisible by 8!");
                }
                else {
                    int verticalTargets[] = new int[MAX_PARTICLES];
                    int horizontalTargets[] = new int[MAX_PARTICLES];
                    int verticalTargetCount, horizontalTargetCount;
                    
                    //iterate through each particle and score to see if it is a target
                    Rect rectangles[] = getRectsFromArray(rectanglesArray);
                    int numParticles = rectangles.length;
                    Scores scores[] = new Scores[numParticles];
                    horizontalTargetCount = verticalTargetCount = 0;
                    
                    for (int i = 0; i < MAX_PARTICLES && i < numParticles; i++) {
                        populateRectangleSizes(rectangles[i]);
                        scores[i] = new Scores();
                        //Score each particle on rectangularity and aspect ratio
                        // set rectangularity to 1.0 for now since we don't have this image/data from NetTable
                        scores[i].rectangularity = 100; //scoreRectangularity(bbWidth, bbHeight);
                        scores[i].aspectRatioVertical = scoreAspectRatio(rectangles[i], true);
                        scores[i].aspectRatioHorizontal = scoreAspectRatio(rectangles[i], false);
                        if (DEBUG_LEVEL >= 3)
                            System.out.println("AspectRatios: " + scores[i].aspectRatioVertical + " " + scores[i].aspectRatioHorizontal);
                        //Check if the particle is a horizontal target, if not, check if it's a vertical target
                        if(scoreCompare(scores[i], false))
                        {
                            if (DEBUG_LEVEL >= 3)
                                System.out.println("particle: " + i + "is a Horizontal Target centerX: " + rectangles[i].center_mass_x + "centerY: " + rectangles[i].center_mass_y);
                            horizontalTargets[horizontalTargetCount++] = i; //Add particle to target array and increment count
                        } else if (scoreCompare(scores[i], true)) {
                            if (DEBUG_LEVEL >= 3)
                                System.out.println("particle: " + i + "is a Vertical Target centerX: " + rectangles[i].center_mass_x + "centerY: " + rectangles[i].center_mass_y);
                            verticalTargets[verticalTargetCount++] = i;  //Add particle to target array and increment count
                        } else {
                            if (DEBUG_LEVEL >= 3)
                                System.out.println("particle: " + i + "is not a Target centerX: " + rectangles[i].center_mass_x + "centerY: " + rectangles[i].center_mass_y);
                        }
                            if (DEBUG_LEVEL >= 3)
                                System.out.println("rect: " + scores[i].rectangularity + "ARHoriz: " + scores[i].aspectRatioHorizontal);
                            if (DEBUG_LEVEL >= 3)
                                System.out.println("ARVert: " + scores[i].aspectRatioVertical);	
                        }
                        //Zero out scores and set verticalIndex to first target in case there are no horizontal targets
                        target.totalScore = target.leftScore = target.rightScore = target.tapeWidthScore = target.verticalScore = 0;
                        target.verticalIndex = verticalTargets[0];
                        for (int i = 0; i < verticalTargetCount; i++)
                        {
                                Rect verticalRect = rectangles[verticalTargets[i]];
                                for (int j = 0; j < horizontalTargetCount; j++)
                                {
                                    Rect horizontalRect = rectangles[horizontalTargets[j]];
                                    double horizWidth, horizHeight, vertWidth, leftScore, rightScore, tapeWidthScore, verticalScore, total;
                                    //Measure equivalent rectangle sides for use in score calculation
                                    horizWidth = horizontalRect.rectLong;
                                    vertWidth = verticalRect.rectShort;
                                    horizHeight = horizontalRect.rectShort;
                                    //Determine if the horizontal target is in the expected location to the left of the vertical target
                                    leftScore = ratioToScore(1.2*(verticalRect.bbLeft - horizontalRect.center_mass_x)/horizWidth);
                                    //Determine if the horizontal target is in the expected location to the right of the  vertical target
                                    rightScore = ratioToScore(1.2*(horizontalRect.center_mass_x - verticalRect.bbLeft - verticalRect.bbWidth)/horizWidth);
                                    //Determine if the width of the tape on the two targets appears to be the same
                                    tapeWidthScore = ratioToScore(vertWidth/horizHeight);
                                    //Determine if the vertical location of the horizontal target appears to be correct
                                    verticalScore = ratioToScore(1-(verticalRect.bbTop - horizontalRect.center_mass_y)/(4*horizHeight));
                                    total = leftScore > rightScore ? leftScore:rightScore;
                                    total += tapeWidthScore + verticalScore;
                                    //If the target is the best detected so far store the information about it
                                    if(total > target.totalScore)
                                    {
                                            target.horizontalIndex = horizontalTargets[j];
                                            target.verticalIndex = verticalTargets[i];
                                            target.totalScore = total;
                                            target.leftScore = leftScore;
                                            target.rightScore = rightScore;
                                            target.tapeWidthScore = tapeWidthScore;
                                            target.verticalScore = verticalScore;
                                    }
                                }
                                //Determine if the best target is a Hot target
                                target.Hot = hotOrNot(target);
                                target.leftHot = leftHot(target);
                                lastTarget = target;
                            }
                            if(verticalTargetCount > 0)
                            {
                                    //Information about the target is contained in the "target" structure
                                    //To get measurement information such as sizes or locations use the
                                    //horizontal or vertical index to get the particle report as shown below
                                    target.distance = computeDistance(rectangles[target.verticalIndex]);
                                    if (DEBUG_LEVEL >= 2) {
                                    if(target.Hot)
                                    {
                                            System.out.println("Hot target located");
                                            System.out.println("Distance: " + target.distance);
                                            System.out.println("LeftHot?: " + target.leftHot);
                                    } else {
                                            System.out.println("No hot target present");
                                            System.out.println("Distance: " + target.distance);
                                    }
                                    }
                                    
                                    target.angle = computeAngle(rectangles[target.verticalIndex]);
                                    netTable.putNumber("VisionDistance", target.distance);
                                    netTable.putNumber("VisionAngle", target.angle);
                                    if (DEBUG_LEVEL >= 2) {
                                        System.out.println("Angle: " + target.angle);
                                        System.out.println("Lateral Distance: " + Math.tan(target.angle) * target.distance);
                                    }
                            }
                        
                }
            }
            return target.distance;
        }
        else {
            return -1;
            //return getDistanceToGoalCrio();
        }
    }
    
    public double getDistanceToGoalCrio() {
	TargetReport target = new TargetReport();
	int verticalTargets[] = new int[MAX_PARTICLES];
	int horizontalTargets[] = new int[MAX_PARTICLES];
	int verticalTargetCount, horizontalTargetCount;
        double distance = -1.0;
        
        try {
            /**
             * Do the image capture with the camera and apply the algorithm described above. This
             * sample will either get images from the camera or from an image file stored in the top
             * level directory in the flash memory on the cRIO. The file name in this case is "testImage.jpg"
             * 
             */
            ColorImage image = camera.getImage();     // comment if using stored images
            //ColorImage image;                           // next 2 lines read image from flash on cRIO
            //image = new RGBImage("/testImage.jpg");		// get the sample image from the cRIO flash
            BinaryImage thresholdImage = image.thresholdHSV(105, 137, 230, 255, 133, 183);   // keep only green objects
            //thresholdImage.write("/threshold.bmp");
            BinaryImage filteredImage = thresholdImage.particleFilter(cc);           // filter out small particles
            //filteredImage.write("/filteredImage.bmp");
            //iterate through each particle and score to see if it is a target
            Scores scores[] = new Scores[filteredImage.getNumberParticles()];
            horizontalTargetCount = verticalTargetCount = 0;
            if(filteredImage.getNumberParticles() > 0)
            {
                    for (int i = 0; i < MAX_PARTICLES && i < filteredImage.getNumberParticles(); i++) {
                    ParticleAnalysisReport report = filteredImage.getParticleAnalysisReport(i);
                    scores[i] = new Scores();
                    //Score each particle on rectangularity and aspect ratio
                    scores[i].rectangularity = scoreRectangularity(report);
                    scores[i].aspectRatioVertical = scoreAspectRatio(filteredImage, report, i, true);
                    scores[i].aspectRatioHorizontal = scoreAspectRatio(filteredImage, report, i, false);			
                    //Check if the particle is a horizontal target, if not, check if it's a vertical target
                    if(scoreCompare(scores[i], false))
                    {
                        System.out.println("particle: " + i + "is a Horizontal Target centerX: " + report.center_mass_x + "centerY: " + report.center_mass_y);
                        horizontalTargets[horizontalTargetCount++] = i; //Add particle to target array and increment count
                    } else if (scoreCompare(scores[i], true)) {
                        System.out.println("particle: " + i + "is a Vertical Target centerX: " + report.center_mass_x + "centerY: " + report.center_mass_y);
                        verticalTargets[verticalTargetCount++] = i;  //Add particle to target array and increment count
                    } else {
                        System.out.println("particle: " + i + "is not a Target centerX: " + report.center_mass_x + "centerY: " + report.center_mass_y);
                    }
                        System.out.println("rect: " + scores[i].rectangularity + "ARHoriz: " + scores[i].aspectRatioHorizontal);
                        System.out.println("ARVert: " + scores[i].aspectRatioVertical);	
                    }
                    //Zero out scores and set verticalIndex to first target in case there are no horizontal targets
                    target.totalScore = target.leftScore = target.rightScore = target.tapeWidthScore = target.verticalScore = 0;
                    target.verticalIndex = verticalTargets[0];
                    for (int i = 0; i < verticalTargetCount; i++)
                    {
                            ParticleAnalysisReport verticalReport = filteredImage.getParticleAnalysisReport(verticalTargets[i]);
                            for (int j = 0; j < horizontalTargetCount; j++)
                            {
                                ParticleAnalysisReport horizontalReport = filteredImage.getParticleAnalysisReport(horizontalTargets[j]);
                                double horizWidth, horizHeight, vertWidth, leftScore, rightScore, tapeWidthScore, verticalScore, total;
                                //Measure equivalent rectangle sides for use in score calculation
                                horizWidth = NIVision.MeasureParticle(filteredImage.image, horizontalTargets[j], false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_LONG_SIDE);
                                vertWidth = NIVision.MeasureParticle(filteredImage.image, verticalTargets[i], false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_SHORT_SIDE);
                                horizHeight = NIVision.MeasureParticle(filteredImage.image, horizontalTargets[j], false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_SHORT_SIDE);
                                //Determine if the horizontal target is in the expected location to the left of the vertical target
                                leftScore = ratioToScore(1.2*(verticalReport.boundingRectLeft - horizontalReport.center_mass_x)/horizWidth);
                                //Determine if the horizontal target is in the expected location to the right of the  vertical target
                                rightScore = ratioToScore(1.2*(horizontalReport.center_mass_x - verticalReport.boundingRectLeft - verticalReport.boundingRectWidth)/horizWidth);
                                //Determine if the width of the tape on the two targets appears to be the same
                                tapeWidthScore = ratioToScore(vertWidth/horizHeight);
                                //Determine if the vertical location of the horizontal target appears to be correct
                                verticalScore = ratioToScore(1-(verticalReport.boundingRectTop - horizontalReport.center_mass_y)/(4*horizHeight));
                                total = leftScore > rightScore ? leftScore:rightScore;
                                total += tapeWidthScore + verticalScore;
                                //If the target is the best detected so far store the information about it
                                if(total > target.totalScore)
                                {
                                        target.horizontalIndex = horizontalTargets[j];
                                        target.verticalIndex = verticalTargets[i];
                                        target.totalScore = total;
                                        target.leftScore = leftScore;
                                        target.rightScore = rightScore;
                                        target.tapeWidthScore = tapeWidthScore;
                                        target.verticalScore = verticalScore;
                                }
                            }
                            //Determine if the best target is a Hot target
                            target.Hot = hotOrNot(target);
                        }
                        if(verticalTargetCount > 0)
                        {
                                //Information about the target is contained in the "target" structure
                                //To get measurement information such as sizes or locations use the
                                //horizontal or vertical index to get the particle report as shown below
                                ParticleAnalysisReport distanceReport = filteredImage.getParticleAnalysisReport(target.verticalIndex);
                                distance = computeDistance(filteredImage, distanceReport, target.verticalIndex);
                                if(target.Hot)
                                {
                                        System.out.println("Hot target located");
                                        System.out.println("Distance: " + distance);
                                } else {
                                        System.out.println("No hot target present");
                                        System.out.println("Distance: " + distance);
                                }
                        }
            }
            /**
             * all images in Java must be freed after they are used since they are allocated out
             * of C data structures. Not calling free() will cause the memory to accumulate over
             * each pass of this loop.
             */
            filteredImage.free();
            thresholdImage.free();
            image.free();
        } catch (AxisCameraException ex) {        // this is needed if the camera.getImage() is called
            ex.printStackTrace();
        } catch (NIVisionException ex) {
            ex.printStackTrace();
        }
        
        return distance;
    }
    
    public NumberArray getRectangles() {
        // Get the bounding boxes (either from NetworkTable or directly)
        if (DEBUG_LEVEL >= 4)
        System.out.println("getRectangles");
        try {
            if (useNetTable) {
                return getRectanglesFromNetTable();
            } else {
                return getRectanglesFromCrio();
            }
        }
	catch (TableKeyNotDefinedException exp)
	{
            return getRectanglesFromCrio();
	}
    }
    /**
     * Computes the estimated distance to a target using the height of the particle in the image. For more information and graphics
     * showing the math behind this approach see the Vision Processing section of the ScreenStepsLive documentation.
     * 
     * @param image The image to use for measuring the particle estimated rectangle
     * @param report The Particle Analysis Report for the particle
     * @param outer True if the particle should be treated as an outer target, false to treat it as a center target
     * @return The estimated distance to the target in Inches.
     */
    double computeDistance (BinaryImage image, ParticleAnalysisReport report, int particleNumber) throws NIVisionException {
            double rectLong, height;
            int targetHeight;
            rectLong = NIVision.MeasureParticle(image.image, particleNumber, false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_LONG_SIDE);
            //using the smaller of the estimated rectangle long side and the bounding rectangle height results in better performance
            //on skewed rectangles
            height = Math.min(report.boundingRectHeight, rectLong);
            targetHeight = 32;
            return Y_IMAGE_RES * targetHeight / (height * 2 * Math.tan(VIEW_ANGLE*Math.PI/(180*2)));
    }
    double computeDistance (Rect verticalRect) {
            double height;
            int targetHeight;
            //using the smaller of the estimated rectangle long side and the bounding rectangle height results in better performance
            //on skewed rectangles
            height = Math.min(verticalRect.bbHeight, verticalRect.rectLong);
            targetHeight = 32;
            return Y_IMAGE_RES * targetHeight / (height * 2 * Math.tan(VIEW_ANGLE*Math.PI/(180*2))); // actually in inches
    }
    double computeAngle (Rect verticalRect) {
        return HORIZONTAL_VIEW_ANGLE * (X_IMAGE_RES / 2 - verticalRect.center_mass_x) / (X_IMAGE_RES);
    }
    
    /**
     * Computes a score (0-100) comparing the aspect ratio to the ideal aspect ratio for the target. This method uses
     * the equivalent rectangle sides to determine aspect ratio as it performs better as the target gets skewed by moving
     * to the left or right. The equivalent rectangle is the rectangle with sides x and y where particle area= x*y
     * and particle perimeter= 2x+2y
     * 
     * @param image The image containing the particle to score, needed to perform additional measurements
     * @param report The Particle Analysis Report for the particle, used for the width, height, and particle number
     * @param outer	Indicates whether the particle aspect ratio should be compared to the ratio for the inner target or the outer
     * @return The aspect ratio score (0-100)
     */
    public double scoreAspectRatio(BinaryImage image, ParticleAnalysisReport report, int particleNumber, boolean vertical) throws NIVisionException
    {
        double rectLong, rectShort, aspectRatio, idealAspectRatio;
        rectLong = NIVision.MeasureParticle(image.image, particleNumber, false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_LONG_SIDE);
        rectShort = NIVision.MeasureParticle(image.image, particleNumber, false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_SHORT_SIDE);
        idealAspectRatio = vertical ? (4.0/32) : (23.5/4);	//Vertical reflector 4" wide x 32" tall, horizontal 23.5" wide x 4" tall
	
        //Divide width by height to measure aspect ratio
        if(report.boundingRectWidth > report.boundingRectHeight){
            //particle is wider than it is tall, divide long by short
            aspectRatio = ratioToScore((rectLong/rectShort)/idealAspectRatio);
        } else {
            //particle is taller than it is wide, divide short by long
            aspectRatio = ratioToScore((rectShort/rectLong)/idealAspectRatio);
        }
	return aspectRatio;
    }
    // Simplified implementation for NetTable processing
    public double scoreAspectRatio(Rect rectangle, boolean vertical)
    {
        double aspectRatio, idealAspectRatio;
        idealAspectRatio = vertical ? (4.0/32) : (23.5/4);	//Vertical reflector 4" wide x 32" tall, horizontal 23.5" wide x 4" tall
	
        if (DEBUG_LEVEL >= 4)
            System.out.println("width, height: " + rectangle.bbWidth + " " + rectangle.bbHeight);
        if (DEBUG_LEVEL >= 4)
            System.out.println("long, short: " + rectangle.rectLong + " " + rectangle.rectShort);
                        
        //Divide width by height to measure aspect ratio
        if(rectangle.bbWidth > rectangle.bbHeight){
            aspectRatio = ratioToScore((rectangle.rectLong/rectangle.rectShort)/idealAspectRatio);
        } else {
            aspectRatio = ratioToScore((rectangle.rectShort/rectangle.rectLong)/idealAspectRatio);
        }
	return aspectRatio;
    }
    
    /**
     * Compares scores to defined limits and returns true if the particle appears to be a target
     * 
     * @param scores The structure containing the scores to compare
     * @param outer True if the particle should be treated as an outer target, false to treat it as a center target
     * 
     * @return True if the particle meets all limits, false otherwise
     */
    boolean scoreCompare(Scores scores, boolean vertical){
	boolean isTarget = true;
	isTarget &= scores.rectangularity > RECTANGULARITY_LIMIT;
	if(vertical){
            isTarget &= scores.aspectRatioVertical > ASPECT_RATIO_LIMIT;
	} else {
            isTarget &= scores.aspectRatioHorizontal > ASPECT_RATIO_LIMIT;
	}
	return isTarget;
    }
    
    /**
     * Computes a score (0-100) estimating how rectangular the particle is by comparing the area of the particle
     * to the area of the bounding box surrounding it. A perfect rectangle would cover the entire bounding box.
     * 
     * @param report The Particle Analysis Report for the particle to score
     * @return The rectangularity score (0-100)
     */
    double scoreRectangularity(ParticleAnalysisReport report){
            if(report.boundingRectWidth*report.boundingRectHeight !=0){
                    return 100*report.particleArea/(report.boundingRectWidth*report.boundingRectHeight);
            } else {
                    return 0;
            }	
    }
    double scoreRectangularity(double particleArea, double boundingRectWidth, double boundingRectHeight){
            if(boundingRectWidth*boundingRectHeight !=0){
                    return 100*particleArea/(boundingRectWidth*boundingRectHeight);
            } else {
                    return 0;
            }	
    }
    
    	/**
	 * Converts a ratio with ideal value of 1 to a score. The resulting function is piecewise
	 * linear going from (0,0) to (1,100) to (2,0) and is 0 for all inputs outside the range 0-2
	 */
	double ratioToScore(double ratio)
	{
		return (Math.max(0, Math.min(100*(1-Math.abs(1-ratio)), 100)));
	}
	
	/**
	 * Takes in a report on a target and compares the scores to the defined score limits to evaluate
	 * if the target is a hot target or not.
	 * 
	 * Returns True if the target is hot. False if it is not.
	 */
	boolean hotOrNot(TargetReport target)
	{
		boolean isHot = true;
		
		isHot &= target.tapeWidthScore >= TAPE_WIDTH_LIMIT;
		isHot &= target.verticalScore >= VERTICAL_SCORE_LIMIT;
		isHot &= (target.leftScore > LR_SCORE_LIMIT) | (target.rightScore > LR_SCORE_LIMIT);
		
		return isHot;
	}
        boolean leftHot(TargetReport target) {
            return target.leftScore > target.rightScore;
        }
}
