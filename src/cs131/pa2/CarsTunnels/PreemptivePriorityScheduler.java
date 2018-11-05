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

public class PreemptivePriorityScheduler extends Tunnel{
	private Collection<Tunnel> 	tunnels = new ArrayList<Tunnel>();
//	public HashMap<BasicTunnel, ArrayList<Vehicle>> tunnelToVehicle = new HashMap<BasicTunnel, ArrayList<Vehicle>>();
	private Lock tryToEnterInner = new ReentrantLock();
	private Condition tunnelNotEmpty = tryToEnterInner.newCondition();
	private Lock ambToAmbLock = new ReentrantLock();
	private Condition AmbulancetoAmbulance = ambToAmbLock.newCondition();
    Comparator<Vehicle> vehicleComparator = new Comparator<Vehicle>() {
        @Override
        public int compare(Vehicle lhs, Vehicle rhs) {
        	return lhs.getPriority()-rhs.getPriority();	

	    }
    };
	PriorityQueue<Vehicle> waiting = new PriorityQueue<Vehicle>(vehicleComparator);
	

	public PreemptivePriorityScheduler(String name,Collection<Tunnel> tunnels, Log log) {
		super(name);
		this.tunnels = tunnels;
		

	}

	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		try {
			System.out.println(vehicle.getName());
			tryToEnterInner.lock();
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
		} finally {
			tryToEnterInner.unlock();
//			ambToAmbLock.unlock(); 
		}
//		if(vehicle instanceof Ambulance) {
//			if(canEnter(vehicle)) {
//				tryToEnterInner.unlock();
//				ambToAmbLock.unlock(); // Unsure about this  - should I be calling lock.unlock() or using finally?
//				return true;
//			}
//			if(vehicle.getTunnel()!=null)
//			while(((BasicTunnel)(vehicle.getTunnel())).ambulance>0) {
//			try {
//				AmbulancetoAmbulance.await();
//				return true;
//				
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			}
//			
//			
//		}
//		if(!(vehicle instanceof Ambulance)) {
//		try {
//			waiting.add(vehicle);
//			while(!vehicle.equals(waiting.peek()) || !canEnter(vehicle)) {
//				System.out.println("In trytoenterinnerfirstloop");
//				try {
//					System.out.println("Waiting for " + vehicle.getName());
//					tunnelNotEmpty.await();
//					
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			waiting.poll();
//			tunnelNotEmpty.signalAll();
//			return true;
//		}finally{
//			tryToEnterInner.unlock();
//			ambToAmbLock.unlock();
//		}
//		}
//		
//		return false;
//		}catch(IllegalMonitorStateException e){
//			System.out.println("ILLEGAL MONITOR");
//			
//		}
//		return false;
	}
	private boolean canEnter(Vehicle vehicle) {
		for(Tunnel tunnel : tunnels) {
			if(tunnel.tryToEnter(vehicle)) {
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
		tryToEnterInner.lock();
//		ambToAmbLock.lock();
		try {
			for(Tunnel t : tunnels) {
				ArrayList<Vehicle> myList = tunnelToVehicle.get(t);
				for(int i = 0; i < myList.size(); i++) {
					if(myList.get(i).equals(vehicle)) {
						t.exitTunnel(vehicle);
						if(!(vehicle instanceof Ambulance)) {
							System.out.println(vehicle.getName() + " Vehicle leaving tunnel");
							tunnelToVehicle.get(t).remove(vehicle);
						}
						if(vehicle instanceof Ambulance) {
							System.out.println("MAde it to exit...now signalling");
//							AmbulancetoAmbulance.signalAll();
							vehicle.getTunnel().ambInTunnel.lock();
							vehicle.getTunnel().ambulanceInTunnel.signalAll();
							vehicle.getTunnel().ambInTunnel.unlock();
//							System.out.println("EXITED TUNNEL YESSS");
						}
						tunnelNotEmpty.signalAll();	
						return;
					}
				}		
			}
		}
		finally {
			tryToEnterInner.unlock();
		}
	}
	

}
