package fs_student;

import robocode.*;
import robocode.util.Utils;

import java.awt.geom.Point2D;
import java.util.Random;

public class RSBot extends TeamRobot
{
    private robocode.RobotStatus myStatus;  //Used for finding direction to fire
    boolean forward;
    double PERCENT_BUFFER = 0.20;
    double enemyEnergy = 100;
    private FailBot.Enemy enemy;

    //Run represents the bot's default behavior
    public void run()
    {
        //Colors: body, gun, radar
        setColors(java.awt.Color.blue, java.awt.Color.black, java.awt.Color.blue);

        setAdjustGunForRobotTurn(true);   //Have gun move independently of robot's turns
        setAdjustRadarForRobotTurn(true);   //Have radar move independently of robot's turns
        setAdjustRadarForGunTurn(true); //Have radar move independently of gun's turns

        forward = true;
        turnRadarRightRadians(Double.POSITIVE_INFINITY);
        double width = getBattleFieldWidth();
        double height = getBattleFieldHeight();
        double wallAvoidDistance = 60;
        double centerX = width / 2;
        double centerY = height / 2;
        double currentDir = getHeading();
        double buffer = PERCENT_BUFFER * Math.max(width, height);

        //Main loop - loop forever
        while(true)
        {
            //this.setMaxVelocity(10);

            double xPos = getX();
            double yPos = getY();

            if(xPos < wallAvoidDistance || xPos > width - wallAvoidDistance)
            {
                reverseDirection();
            }
            else if(yPos < wallAvoidDistance || yPos > height - wallAvoidDistance)
            {
                reverseDirection();
            }

            if(yPos < buffer)   //Too close to the bottom
            {
                if(getHeading() < 180)
                {
                    setTurnLeft(90);
                }
                else
                {
                    setTurnRight(90);
                }
            }
            else if(yPos > height - buffer)  //Too close to the top
            {
                if(getHeading() < 90)
                {
                    setTurnRight(90);
                }
                else
                {
                    setTurnLeft(90);
                }
            }
            else    //Go straight forward
            {
                setTurnRight(0);
                setTurnLeft(0);
            }
            setAhead(10);

            if(getRadarTurnRemaining() == 0)
            {
                setTurnRadarRightRadians(Double.POSITIVE_INFINITY);    //Scan for enemies
            }
            //turnRight(90);

            scan();
            execute();
        }
    }

    //onScannedRobot tells the bot what to do when another robot is scanned/seen
    public void onScannedRobot(ScannedRobotEvent e)
    {
        //Power of 1 - Travels fastest, but does the least damage
        //Power of 2 - Travels slower, but does more damage than 1
        //Power of 3 - Travels slowest, but does the most damage

        double moveDir = getHeading();

        //Angle towards target
        double angleToEnemy = getHeadingRadians() + e.getBearingRadians();

        moveDir = angleToEnemy;

        //Subtract current radar heading to get turn required to face enemy - be sure it's normalized
        double radarTurn = Utils.normalRelativeAngle(angleToEnemy - getRadarHeadingRadians());
        //Subtract current gun heading to get turn required to face enemy - be sure it's normalized
        //double gunTurn = Utils.normalRelativeAngle(angleToEnemy - getGunHeadingRadians());

        //Distance we want to scan from middle of enemy to either side
        //The 36.0 is how many units from the center of the enemy robot it scans
        double extraTurnRadar = Math.min(Math.atan(36.0 / e.getDistance()), Rules.RADAR_TURN_RATE_RADIANS);
        //double extraTurnGun = Math.min(Math.atan(36.0 / e.getDistance()), Rules.GUN_TURN_RATE_RADIANS);

        //Adjust the radar turn so it goes that much further in the direction it is going to turn
        //If we want to turn it left, turn it even more left. If right, turn it even more right
        //This allows us to overshoot our enemy so that we get a good sweep that will not slip
        if(radarTurn < 0)
        {
            radarTurn -= extraTurnRadar;
        }
        else
        {
            radarTurn += extraTurnRadar;
        }
        //if(gunTurn < 0) //Same for gun
        //{
        //    gunTurn -= extraTurnGun;
        //}
        //else
        //{
        //    gunTurn += extraTurnGun;
        //}

        //Turn the radar
        setTurnRadarRightRadians(radarTurn);
        //Turn the gun
        //setTurnGunRightRadians(gunTurn);

        setTurnRight(e.getBearing());
        setAhead(1000);
        //if(getTime() % 20 == 0) //Strafe by changing direction every 20 ticks
        //{
        //    moveDir *= -1;
        //    setBack(150 * moveDir);
        //}
        if(e.getEnergy() < 100)
        {
            ////Turn either left or right and start moving that way
            //Random rand = new Random();
            ////int upperbound = 1;
            //float randFlt = rand.nextFloat();
            //if(randFlt < 0.5)
            //{
            //    turnLeft(70);
            //}
            //else if(randFlt >= 0.5)
            //{
            //    turnRight(70);
            //}

            moveDir *= -1;
        }

        //Find enemy energy
        double energyOffset = enemyEnergy - e.getEnergy();
        enemyEnergy = e.getEnergy();

        ////Calculate firepower based on distance
        //double firePower = Math.min(500 / e.getDistance(), 3);
        ////Calculate speed of bullet
        //double bulletVel = 20 - firePower * 3;
        ////Distance = rate * time, solved for time
        //long time = (long)(e.getDistance() / bulletVel);
//
        ////Calculate gun turn to predicted x, y position
        //double futureX = myStatus.getX() + Math.sin(Math.toRadians(getHeading())) * getVelocity() * time;
        //double futureY = myStatus.getY() + Math.sin(Math.toRadians(getHeading())) * getVelocity() * time;
        //double absDeg = absoluteBearing(getX(), getY(), futureX, futureY);
//
        ////Turn gun to predicted x, y location
        //setTurnGunRight(normalizeBearing(absDeg - getGunHeading()));
//
        ////If gun is cool and pointed in the right direction, fire
        //if(getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10)
        //{
        //    setFire(firePower);
        //}

        fire(3);
        //if(e.getDistance() < 600)
        //{
        //    fire(1);
        //    if(e.getDistance() < 300)
        //    {
        //        fire(2);
        //    }
        //    else if(e.getDistance() < 100)
        //    {
        //        fire(3);
        //    }
        //}

        execute();
    }

    //onHitByBullet determines what the robot does when it's hit by a bullet
    //public void onHitByBullet(HitByBulletEvent e)
    //{
    //    //Turn either left or right and start moving that way
    //    Random rand = new Random();
    //    //int upperbound = 1;
    //    float randFlt = rand.nextFloat();
    //    if(randFlt < 0.5)
    //    {
    //        turnLeft(70);
    //    }
    //    else if(randFlt >= 0.5)
    //    {
    //        turnRight(70);
    //    }

    //    //Increase speed to avoid being shot again
    //    this.setMaxVelocity(20);
    //    waitFor(new MoveCompleteCondition(this));   //When the move is complete, return to normal speed
    //    this.setMaxVelocity(10);
    //}

    double absoluteBearing(double x1, double y1, double x2, double y2)
    {
        double xo = x2 - x1;
        double yo = y2 - y1;
        double hyp = Point2D.distance(x1, y1, x2, y2);
        double arcSin = Math.toDegrees(Math.asin(xo / hyp));
        double bearing = 0;

        if(xo > 0 && yo > 0)    //Both positive = lower left
        {
            bearing = arcSin;
        }
        else if(xo < 0 && yo > 0)   //X negative, y positive = lower right
        {
            bearing = 360 + arcSin;
        }
        else if(xo > 0 && yo < 0)   //X positive, y negative = upper left
        {
            bearing = 180 - arcSin;
        }
        else if(xo < 0 && yo < 0)   //Both negative = upper right
        {
            bearing = 180 - arcSin;
        }
        return bearing;
    }

    //Normalizes a bearing to between +180 and -180
    double normalizeBearing(double angle)
    {
        while (angle >  180)
        {
            angle -= 360;
        }
        while (angle < -180)
        {
            angle += 360;
        }
        return angle;
    }

    public void reverseDirection()
    {
        if (forward == true)    //If bot is moving forward, make it move backward
        {
            setBack(40000);
            forward = false;
        }
        else    //If bot is moving backward, make it move forward
        {
            setAhead(40000);
            forward = true;
        }
    }

    //onHitWall determines what to do if the bot hits the wall
    public void onHitWall(HitWallEvent e)
    {
        //If the bot hit the wall, reverse its direction
        reverseDirection();
    }

    public void onHitRobot(HitRobotEvent e)
    {
        //If we ran into the other bot, reverse
        //if(e.isMyFault())
        //{
        //    reverseDirection();
        //}

        setBack(200);
    }
}
