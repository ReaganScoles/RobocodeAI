package fs_student;

import robocode.*;
import robocode.util.Utils;

import java.util.Random;

public class RSBot extends TeamRobot
{
    private robocode.RobotStatus myStatus;  //Used for finding direction to fire
    boolean forward;
    double enemyEnergy = 100;

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

        //Main loop - loop forever
        while(true)
        {
            //this.setMaxVelocity(10);

            //Random rand = new Random();
            //Random rand = new Random();
            //int upperbound = 10;
            //int upperbound = 10;
            //int randNum = rand.nextInt(upperbound);
            //int randNum = rand.nextInt(upperbound);

            //ahead(randNum * 10);       //Move forward by a large amount
            if(getRadarTurnRemaining() == 0)
            {
                setTurnRadarRightRadians(Double.POSITIVE_INFINITY);    //Scan for enemies
            }
            //turnRight(90);

            scan();
            execute();

            //stop(); - causes the robot to stop until resume() is called
            //resume(); - causes the robot to continue moving
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
        setBack(200);

        //Subtract current radar heading to get turn required to face enemy - be sure it's normalized
        double radarTurn = Utils.normalRelativeAngle(angleToEnemy - getRadarHeadingRadians());
        //Subtract current gun heading to get turn required to face enemy - be sure it's normalized
        double gunTurn = Utils.normalRelativeAngle(angleToEnemy - getGunHeadingRadians());

        //Distance we want to scan from middle of enemy to either side
        //The 36.0 is how many units from the center of the enemy robot it scans
        double extraTurnRadar = Math.min(Math.atan(36.0 / e.getDistance()), Rules.RADAR_TURN_RATE_RADIANS);
        double extraTurnGun = Math.min(Math.atan(36.0 / e.getDistance()), Rules.GUN_TURN_RATE_RADIANS);

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
        if(gunTurn < 0) //Same for gun
        {
            gunTurn -= extraTurnGun;
        }
        else
        {
            gunTurn += extraTurnGun;
        }

        //Turn the radar
        setTurnRadarRightRadians(radarTurn);
        //Turn the gun
        setTurnGunRightRadians(gunTurn);

        setTurnRight(e.getBearing());
        //setAhead(1000);
        if(getTime() % 20 == 0) //Strafe by changing direction every 20 ticks
        {
            moveDir *= -1;
            setAhead(150 * moveDir);
        }

        //Find enemy energy
        double energyOffset = enemyEnergy - e.getEnergy();
        enemyEnergy = e.getEnergy();

        //Find enemy bullet velocity
        double bulletVel = 20 - 3 * energyOffset;

        if(e.getDistance() < 600)
        {
            fire(1);
            if(e.getDistance() < 300)
            {
                fire(2);
            }
            else if(e.getDistance() < 100)
            {
                fire(3);
            }
        }

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

    //public void onHitRobot(HitRobotEvent e)
    //{
    //    //If we ran into the other bot, reverse
    //    if(e.isMyFault())
    //    {
    //        reverseDirection();
    //    }
    //}
}
