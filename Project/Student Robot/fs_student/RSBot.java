package fs_student;

import robocode.*;
import robocode.util.Utils;

import java.util.Random;

public class RSBot extends TeamRobot
{
    private robocode.RobotStatus myStatus;  //Used for finding direction to fire
    boolean forward;

    //Run represents the bot's default behavior
    public void run()
    {
        //Colors: body, gun, radar
        setColors(java.awt.Color.blue, java.awt.Color.black, java.awt.Color.blue);

        setAdjustGunForRobotTurn(true);   //Have gun move independently of robot's turns
        setAdjustRadarForRobotTurn(true);   //Have radar move independently of robot's turns
        //setAdjustRadarForGunTurn(true); //Have radar move independently of gun's turns

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
            //turnRadarRight(360);     //Scan for enemies
            //if(getRadarTurnRemaining() == 0)
            //{
            //    turnRadarRightRadians(Double.POSITIVE_INFINITY);
            //}
            //turnRight(90);
            setTurnRadarRight(360);

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

        //Angle towards target
        double angleToEnemy = getHeading() - getRadarHeading() + e.getBearing();

        //Subtract current radar heading to get turn required to face enemy - be sure it's normalized
        //double radarTurn = Utils.normalRelativeAngle(angleToEnemy - getRadarHeading());

        //setTurnRadarLeftRadians(getRadarTurnRemainingRadians());    //Lock onto enemy
        //setTurnRadarLeftRadians(Utils.normalRelativeAngle(radarTurn));
        setTurnRadarRight(angleToEnemy);
        //setTurnGunRightRadians(radarTurn);
        //setTurnGunRight(angleToEnemy);
        setTurnGunLeft(angleToEnemy);

        //setTurnRight(e.getBearing() + 90);
        //setAhead(1000);

        //setTurnGunRight(e.getBearing());
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
