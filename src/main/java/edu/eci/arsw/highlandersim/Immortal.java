package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback = null;
    private final AtomicInteger health;
    private final int defaultDamageValue;
    private final Object lock;
    private final CountDownLatch countDownLatch;
    private final List<Immortal> immortalsPopulation;
    private final String name;
    private final Random r = new Random(System.currentTimeMillis());

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue,
            ImmortalUpdateReportCallback ucb, Object lock, CountDownLatch countDownLatch) {
        super(name);
        this.updateCallback = ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = new AtomicInteger(health);
        this.defaultDamageValue = defaultDamageValue;
        this.lock = lock;
        this.countDownLatch = countDownLatch;
    }

    public void run() {
        while (getHealth() > 0 && countDownLatch.getCount() > 1) {
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
            boolean actionPerformed = false;
            while (!actionPerformed && countDownLatch.getCount() > 1) {
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
                        if (im.getHealth() <= 0)
                            continue;
                        if (getHealth() <= 0) {
                            actionPerformed = true;
                            continue;
                        }
                        this.fight(im);
                        actionPerformed = true;
                    }
                }
            }
        }
        if (countDownLatch.getCount() == 1 && getHealth() > 0) {
            updateCallback.processReport("The winner is: " + this + "\n");
        } else {
            countDownLatch.countDown();
        }

    }

    public void fight(Immortal i2) {
        int myHealth = getHealth();
        int i2Health = i2.getHealth();
        if (i2Health > 0) {
            i2.setHealth(i2Health - defaultDamageValue);
            setHealth(myHealth + defaultDamageValue);
            updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
            if (i2.getHealth() == 0) {
                updateCallback
                        .processReport(this + " kill " + i2 + " and there is " + countDownLatch.getCount() + " left\n");
            }
        } else {
            updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
        }
    }

    public int getHealth(){
        return health.get();
    }

    public void setHealth(int newHealth){
        health.set(newHealth);
    }

    @Override
    public String toString() {
        return name + "[" + getHealth() + "]";
    }

}
