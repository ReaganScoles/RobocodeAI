package fs_student;

import robocode.*;
import robocode.util.Utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import robocode.util.Utils;
import java.util.Random;

public class RSBot extends TeamRobot
{
    static int currentEnemyVelocity;
    static int aimingEnemyVelocity;
    double velocityToAimAt;
    boolean fired;
    boolean forward;
    double prevTime;
    int count;
    int averageCount;
    //Array is used to store enemy velocity at different ticks
    //  The first index represents number of ticks since game started (or since last reset of this value)
    //  The second index is an int representation of enemy's velocity on a particular tick
    static double enemyVelocities[][] = new double[500][4];
    static double turn = 2;
    int turnDir = 1;
    int moveDir = 1;
    double prevEnemyHeading;
    double prevEnergy = 100;

    //Run represents the bot's default behavior
    public void run()
    {
        //Set bot colors
        setBodyColor(Color.blue);
        setGunColor(Color.blue);
        setRadarColor(Color.blue);
        setScanColor(Color.blue);

        setAdjustGunForRobotTurn(true); //Make gun movement independent of bot turning
        setAdjustRadarForGunTurn(true); //Make radar movement independent of gun turning

        forward = true;

        //Main while loop
        while (true)
        {
            //Scan
            turnRadarRightRadians(Double.POSITIVE_INFINITY);
        }
    }

    //onScannedRobot determines what the bot does when it scans an enemy
    public void onScannedRobot(ScannedRobotEvent e)
    {
        //Get bearing to use for targeting - value between enemy's relation to up and its direction - helps get enemy position
        double absBearing = e.getBearingRadians() + getHeadingRadians();

        //Graphics2D g = getGraphics();

        //How much do we need to turn?
        turn += 0.2 * Math.random();    //Do random strafing to confuse enemy predictive targeting
        if (turn > 8)
        {
            turn = 2;
        }

        //If enemy's energy gets between a certain amount (meaning enemy fired), randomize movement and turning
        if(prevEnergy - e.getEnergy() <= 3 && prevEnergy - e.getEnergy() >= 0.1)
        {
            if (Math.random() > .5) //Randomize turning
            {
                turnDir *= -1;
            }
            if(Math.random() > .8)  //Randomize movement
            {
                moveDir *= -1;
            }
        }

        //Limit how much bot can turn
        setMaxTurnRate(turn);
        //Limit how fast the bot can move - pixels/turn
        setMaxVelocity(12 - turn);
        setAhead(90 * moveDir);
        setTurnLeft(90 * turnDir);
        //Update default energy used to find if enemy fired
        prevEnergy = e.getEnergy();

        if (e.getVelocity() < -2)   //Reset index of enemy velocity to default
        {
            currentEnemyVelocity = 0;
        }
        else if (e.getVelocity() > 2)   //Update enemy velocity index
        {
            currentEnemyVelocity = 1;
        }
        else if (e.getVelocity() <= 2 && e.getVelocity() >= -2)
        {
            if (currentEnemyVelocity == 0)  //Update enemy velocity index
            {
                currentEnemyVelocity = 2;
            }
            else if (currentEnemyVelocity == 1) //Update enemy velocity index
            {
                currentEnemyVelocity = 3;
            }
        }
        //If game time is greater than enemy distance from bot (12 is fine-tuning), and if enemy fired
        if (getTime() - prevTime > e.getDistance() / 12 && fired == true)
        {
            aimingEnemyVelocity = currentEnemyVelocity;   //Update index used to store enemy velocities
        }
        else    //Otherwise, enemy didn't fire
        {
            fired = false;
        }

        enemyVelocities[count][aimingEnemyVelocity] = e.getVelocity();  //Store enemy's current velocity this tick
        count++;    //Increment index that represents which tick corresponds with enemy's velocity

        if(count == 500)    //If it's been a while, reset index
        {
            count = 0;
        }

        averageCount = 0;       //Index to represent which tick we should get enemy velocity from for following calculation
        velocityToAimAt = 0;    //Representation of where bot should aim

        while(averageCount < 500)
        {
            //Velocity bot aims at should be offset by enemy's current velocity
            velocityToAimAt += enemyVelocities[averageCount][currentEnemyVelocity];
            averageCount++; //Constantly update index to get current enemy velocity for next tick
        }

        velocityToAimAt /= 500;

        //Get minimum of enemy's energy and bot's energy, get minimum of that and 2 (fine-tuning), make that bullet power
        double bulletPower = Math.min(2, Math.min(e.getEnergy(), getEnergy()));
        double myX = getX();
        double myY = getY();
        double enemyX = getX() + e.getDistance() * Math.sin(absBearing);    //xPos = x + distToEnemy * sinOfEnemyBearing
        double enemyY = getY() + e.getDistance() * Math.cos(absBearing);    //yPos = y + distToEnemy * cosOfEnemyBearing
        double enemyHeading = e.getHeadingRadians();
        double enemyHeadingChange = enemyHeading - prevEnemyHeading; //Store change in enemy's direction
        prevEnemyHeading = enemyHeading; //Update previous direction for use next tick
        double deltaTime = 0;
        double battleFieldHeight = getBattleFieldHeight();
        double battleFieldWidth = getBattleFieldWidth();
        double predictedX = enemyX;
        double predictedY = enemyY;

        //Loop while elapsed time * bullet power calculation < distance between bot's position and predicted enemy position
        while ((deltaTime++) * (20.0 - 3.0 * bulletPower) < Point2D.Double.distance( myX, myY, predictedX, predictedY))
        {
            //Increment predicted enemy xPos by sin of enemy direction * calculated lead
            predictedX += Math.sin(enemyHeading) * velocityToAimAt;
            //Increment predicted enemy yPos by cos of enemy direction * calculated lead
            predictedY += Math.cos(enemyHeading) * velocityToAimAt;
            //Increment representation of enemy's direction by the change in its direction
            enemyHeading += enemyHeadingChange;

            //If predicted enemy position is too close to the walls, update predicted enemy position and break out of loop
            if (predictedX < 15 || predictedY < 15 || predictedX > battleFieldWidth - 15 || predictedY > battleFieldHeight - 15)
            {
                //Predicted xPos = minimum of the maximum of 15 and predicted xPos, and the maximum battlefield width - 15
                predictedX = Math.min(Math.max(15, predictedX), battleFieldWidth - 15);
                //Predicted yPos = minimum of the maximum of 15 and predicted yPos, and the maximum battlefield height - 15
                predictedY = Math.min(Math.max(15, predictedY), battleFieldHeight - 15);
                break;
            }
        }

        //Get angle between the difference of predicted xPos and bot xPos, and predicted yPos and bot yPos
        double theta = Utils.normalAbsoluteAngle(Math.atan2(predictedX - getX(), predictedY - getY()));
        //Turn the radar to face enemy
        //Normalize difference between enemy bearing and radar's direction * 2
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * 2);
        //Turn the gun to face enemy
        //Normalize above angle - gun's direction
        setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));

        //If gun is cooled down, fire based on calculated bullet power
        if(getGunHeat() == 0)
        {
            fire(bulletPower);
            fired = true;
        }
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
        double hyp = Point2D.distance(x1, y1, x2, y2);  //Distance between point 1 and point 2
        double arcSin = Math.toDegrees(Math.asin(xo / hyp));    //Inverse of sine of xPos difference and distance between points
        double bearing = 0; //Bearing to return

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
    double normalizeBearing(double bearing)
    {
        while (bearing > 180)
        {
            bearing -= 360;
        }
        while (bearing < -180)
        {
            bearing += 360;
        }

        return bearing;
    }

    public void reverseDirection()
    {
        if (forward == true)    //If bot is moving forward, make it move backward quite a bit
        {
            setBack(5000);
            forward = false;
        }
        else    //If bot is moving backward, make it move forward quite a bit
        {
            setAhead(5000);
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
