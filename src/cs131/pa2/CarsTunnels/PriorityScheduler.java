package cs131.pa2.CarsTunnels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Log;

public class PriorityScheduler extends Tunnel{
	private Collection<Tunnel> 	tunnels = new ArrayList<Tunnel>();
//	public HashMap<BasicTunnel, ArrayList<Vehicle>> tunnelToVehicle = new HashMap<BasicTunnel, ArrayList<Vehicle>>();
	private Lock lock = new ReentrantLock();
	private Condition tunnelNotEmpty = lock.newCondition();
    Comparator<Vehicle> vehicleComparator = new Comparator<Vehicle>() {
        @Override
        public int compare(Vehicle lhs, Vehicle rhs) {
        	return rhs.getPriority()-lhs.getPriority();	
//	        if (lhs.getPriority() < rhs.getPriority()) return 1; 
//	        if (lhs.getPriority() == (rhs.getPriority())) return 0;
//	        return -1;
	    }
    };
	PriorityQueue<Vehicle> waiting = new PriorityQueue<Vehicle>(vehicleComparator);
	

	public PriorityScheduler(String name,Collection<Tunnel> tunnels, Log log) {
		super(name);
		this.tunnels = tunnels;
		

	}

	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		lock.lock();
		try {
			waiting.add(vehicle);
			while(!vehicle.equals(waiting.peek()) || !canEnter(vehicle)) {
				try {
					tunnelNotEmpty.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			waiting.poll();
			tunnelNotEmpty.signalAll();
			return true;
		}finally{
			lock.unlock();
		}
	}
	private boolean canEnter(Vehicle vehicle) {
		for(Tunnel tunnel : tunnels) {
			if(tunnel.tryToEnterInner(vehicle)) {
				vehicle.setTunnel(tunnel);
				if(tunnelToVehicle.get(tunnel) != null) {
					tunnelToVehicle.get(tunnel).add(vehicle);
				}
				else{
					tunnelToVehicle.put((BasicTunnel)tunnel, new ArrayList<Vehicle>());
					tunnelToVehicle.get(tunnel).add(vehicle);
				}
				return true;
			}
		}
		return false;
	}
	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		lock.lock();
		try {
			for(Tunnel t : tunnels) {
				ArrayList<Vehicle> myList = tunnelToVehicle.get(t);
				for(int i = 0; i < myList.size(); i++) {
					if(myList.get(i).equals(vehicle)) {
						t.exitTunnel(vehicle);
						tunnelToVehicle.get(t).remove(vehicle);
						tunnelNotEmpty.signalAll();	
						return;
					}
				}		
			}
		}
		finally {
			lock.unlock();
		}
	}
	

}
