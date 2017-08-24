import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;

public class DataStore 
{
	public static enum type_map { DS_Trips, DS_TS, DS_Shed, DS_Time};

	public Map<String, String> mapTrips_ = new ConcurrentHashMap<String, String>(); 
	public Map<String, String> mapTS_ = new ConcurrentHashMap<String, String>(); 
	public Map<String, String> mapShed_ = new ConcurrentHashMap<String, String>(); 
	public Map<String, String> mapTime_ = new ConcurrentHashMap<String, String>(); 

	public Map<String, OpCoord> mapOPByStopID_ = new ConcurrentHashMap<String, OpCoord>(); 	
	public Map<Integer, HashSet<OpCoord>> mapOpByQuad_ = new ConcurrentHashMap<Integer, HashSet<OpCoord>> ();
	
	public void putAll (DataStore dsSrc)
	{
		if(dsSrc == null)
			return;
		
		mapTrips_.putAll (dsSrc.mapTrips_); 
		mapTS_.putAll (dsSrc.mapTS_); 
		mapShed_.putAll (dsSrc.mapShed_); 
		mapTime_.putAll (dsSrc.mapTime_); 
		
		mapOPByStopID_.putAll(dsSrc.mapOPByStopID_);
	}
}
