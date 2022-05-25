package fs_student;

import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;
import java.util.Random;

public class RSBot extends TeamRobot
{
    private robocode.RobotStatus myStatus;  //Used for finding direction to fire

    //Run represents the bot's default behavior
    public void run()
    {
        //Colors: body, gun, radar
        setColors(java.awt.Color.blue, java.awt.Color.black, java.awt.Color.blue);

        //Main loop - loop forever
        while(true)
        {
            //setAdjustGunForRobotTurn(true);   //Have gun move independently of robot's turns
            //setAdjustRadarForRobotTurn(true);   //Have radar move independently of robot's turns
            //setAdjustRadarForGunTurn(true); //Have radar move independently of gun's turns

            ahead(500);
            turnGunRight(360);
            back(500);
            turnGunRight(360);

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

        //If the robot is close, fire; Otherwise, don't fire
        if(e.getDistance() < 600)
        {
            if(getOthers() < 2) //If there's only one other bot left in the game
            {
                double enemyAngle = e.getBearing();
                double angleToFire = Math.toRadians(myStatus.getHeading() + enemyAngle % 360);
                double enemyXPos = (myStatus.getX() + Math.sin(angleToFire) * e.getDistance());
                double enemyYPos = (myStatus.getY() + Math.sin(angleToFire) * e.getDistance());

                turnGunRight(angleToFire);
            }

            //Fire with a power of 1
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
    }

    //onHitByBullet determines what the robot does when it's hit by a bullet
    public void onHitByBullet(HitByBulletEvent e)
    {
        //Increase speed to avoid being shot again
        this.setMaxVelocity(1000);

        //Turn either left or right and start moving that way
        Random rand = new Random();
        //int upperbound = 1;
        float randFlt = rand.nextFloat();
        if(randFlt < 0.5)
        {
            turnLeft(70);
        }
        else if(randFlt >= 0.5)
        {
            turnRight(70);
        }
    }

    //onHitWall determines what to do if the bot hits the wall
    public void onHitWall(HitWallEvent e)
    {
        //Based on the bearing of the wall, turn toward that heading
        double wallBearing = e.getBearing();

        if(this.getHeading() > 0)
        {
            back(100);
            turnRight(-wallBearing);
        }
        else
        {
            turnRight(180);
            back(100);
            turnRight(wallBearing);
        }
    }
}
