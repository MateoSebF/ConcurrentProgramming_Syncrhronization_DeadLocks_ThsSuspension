/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arst.concprg.prodcons;

import java.util.Queue;

/**
 *
 * @author hcadavid
 */
public class Consumer extends Thread{
    
    private Queue<Integer> queue;
    
    
    public Consumer(Queue<Integer> queue){
        this.queue=queue;        
    }
    
    @Override
    public void run() {
        while (true) {
            synchronized (queue) {
                try {
                    if(queue.size()==0){
                        queue.wait();
                        System.out.println("Me notificaron que llego un producto");
                    }

                } catch (Exception e) {
                }
            }
                while (queue.size() > 0) {
                    synchronized (queue){
                        int elem=queue.poll();
                        queue.notifyAll();
                        System.out.println("Consumer consumes "+elem);
                    }
                    try{
                        Thread.sleep(100);
                    }
                    catch (Exception e){
                    }
                }
        }
    }
}
