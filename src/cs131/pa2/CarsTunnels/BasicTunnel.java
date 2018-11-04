package cs131.pa2.CarsTunnels;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs131.pa2.Abstract.Direction;
import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;

public class BasicTunnel extends Tunnel{
		int car = 0;
		int sled = 0;
		Direction direction = null;
		public int ambulance = 0;
		public Lock ambOutTunnel =  new ReentrantLock();
		public Condition ambulanceOutTunnel = ambOutTunnel.newCondition();
		public Lock ambInTunnel =  new ReentrantLock();
		public Condition ambulanceInTunnel = ambInTunnel.newCondition();
		
		
	public BasicTunnel(String name) {
		super(name);
	}

	@Override
	public synchronized boolean tryToEnterInner(Vehicle vehicle) {
		if(vehicle instanceof Ambulance )
			if(ambulance>0) {
				return false;
			}else {
				
				ambulance++;
				return true;
			}
		if(ambulance>0) {
			return false;
		}
		
		if((car==0 && sled==0)) {
			if(vehicle instanceof Car) {
				car++;
			}else {
				sled++;
			}
			direction = vehicle.getDirection();
			return true;
		}else if(vehicle instanceof Sled) {
			return false;
			
		}else if(car<3 && sled==0) {
			if(vehicle.getDirection().equals(direction)) {
				car++;
				return true;
			}else {
				return false;
			}
		}
		return false;
	}
		
		

	@Override
	public synchronized void exitTunnelInner(Vehicle vehicle) {
		if(vehicle instanceof Car) {
			car--;
		}else if(vehicle instanceof Sled) {
			sled--;
		}else {
//			try {
//			this.ambInTunnel.lock();
//     		 this.ambulanceInTunnel.signalAll();
// 		 System.out.println("Amburlance has exited the tunnel");
//  		     this.ambInTunnel.unlock();
//			}catch(IllegalMonitorStateException e) {
//				System.out.println("ILLEGAL MONITOR");
//			}
			ambulance--;
		}
		if(car==0 && sled==0) {
			direction=null;
		}
	}
	
}
