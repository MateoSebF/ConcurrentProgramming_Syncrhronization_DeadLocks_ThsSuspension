package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback = null;

    private int health;

    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    public boolean isAlive = true;

    private final Random r = new Random(System.currentTimeMillis());
    private Object lock;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue,
            ImmortalUpdateReportCallback ucb, Object lock) {
        super(name);
        this.updateCallback = ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue = defaultDamageValue;
        this.lock = lock;
    }

    public void run() {
        while (health > 0 && ControlFrame.inmortalNumber > 1) {

            synchronized (lock) {
                while (ControlFrame.isPaused) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            Immortal im;

            int myIndex = immortalsPopulation.indexOf(this);
            int nextFighterIndex;

            do {
                nextFighterIndex = r.nextInt(immortalsPopulation.size());

                // avoid self-fight
                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
                }

                im = immortalsPopulation.get(nextFighterIndex);

                Object firstLock = myIndex < nextFighterIndex ? this : im;
                Object secondLock = myIndex < nextFighterIndex ? im : this;
    
                synchronized (firstLock) {
                    synchronized (secondLock) {
                        if(im.isAlive){
                            this.fight(im);
                            break;
                        }
                    }
                }

            } while (!im.isAlive && ControlFrame.inmortalNumber > 1);



        }
        if (ControlFrame.inmortalNumber == 1 && this.isAlive) {
            updateCallback.processReport("The winner is: " + this + "\n");
        } 

    }

    public void fight(Immortal i2) {
        if (i2.getHealth() > 0) {
            i2.changeHealth(i2.getHealth() - defaultDamageValue);
            this.health += defaultDamageValue;
            updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
        } else {
            updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
        }

    }

    public void changeHealth(int v) {
        
        health = v;
        if(v == 0){
            isAlive = false;
            ControlFrame.decreaseInmortalNumber();
        }
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
