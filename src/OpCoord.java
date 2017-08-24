import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OpCoord extends Coordinate
{
	final public static String tagOpStopId_ = "stop_id";
	final public static String tagOpName_ = "name";
	final public static String tagOpLon_ = "lon";
	final public static String tagOpLat_ = "lat";
	final public static String tagOpDisf_ = "disf";
	
	final public String opStopId_;
	final public String opName_;
	final public String opDisf_;

	public OpCoord (String json) throws Exception
	{
		super(Double.parseDouble(Utils.getJSonValue(json, tagOpLat_)), 
				Double.parseDouble(Utils.getJSonValue(json, tagOpLon_)));
		opStopId_ = Utils.getJSonValue(json, tagOpStopId_);
		opName_ = Utils.getJSonValue(json, tagOpName_).replace("\\", "").replaceAll("\"", " ");
		opDisf_ = Utils.getJSonValue(json, tagOpDisf_);
	}
	
	public String getInfoCsv()
	{
//		Широта;Долгота;Описание;Подпись
		String sRes = "";
		sRes += dLat_ + ";";
		sRes += dLon_ + ";";
		sRes += opName_ + ";";
		sRes += opStopId_ + ";";
		
		return sRes;
	}

	public String getInfo()
	{
		String sRes = " nQuad=" + nQuad_;
		sRes += " opStopId=" + opStopId_;
		sRes += " opName=" + opName_;
		sRes += " opLat=" + dLat_;
		sRes += " opLon=" + dLon_;
		if(opDisf_ != null)
			sRes += " opDisf=" + opDisf_;
		
		return sRes;
	}
	
	public static boolean LoadCoorOP(String jsonResp, 
			Map<Integer, HashSet<OpCoord>> mapOpByQuad, 
			Map<String, OpCoord> mapOPByStopID)
	{
		if(jsonResp == null || jsonResp.isEmpty())
			return false;

		String separator = "\\{";
		for(String sLine: jsonResp.split(separator))
		{
			OpCoord opCur = null;
			try {
				opCur = new OpCoord(sLine);
			} catch(Exception e)
			{
				Core.log_.error("Can't load opCoor for json:" + sLine);
				Core.log_.error(e);
				continue;
			}
			
			if(opCur.opStopId_ == null || opCur.opStopId_.isEmpty())
			{
				Core.log_.error("opStopId is empty: " + opCur.getCoordInfo());
				continue;
			}
			
			OpCoord opOld = mapOPByStopID.get(opCur.opStopId_);
			if(opOld != null)
			{
				Core.log_.info("StopID already found: " + opCur.opStopId_);
				continue;
			}
			mapOPByStopID.put(opCur.opStopId_, opCur);
						
			HashSet<OpCoord> set = mapOpByQuad.get(opCur.nQuad_);
			if (set == null)
				set = new HashSet<OpCoord> ();
			set.add(opCur);
			mapOpByQuad.put(opCur.nQuad_, set);
		}
		return !mapOPByStopID.isEmpty();
	}
	
/*
	public static boolean AddAll (Map<Integer, HashSet<? extends Coordinate>> mapRes, Map<Integer, HashSet<? extends Coordinate>> mapNew)
	{
		if(mapRes == null || mapNew == null)
			return false;
		
		for(Integer quad : mapNew.keySet())
		{			
			HashSet<? extends Coordinate> setRes = mapRes.get(quad);
			HashSet<? extends Coordinate> setNew = mapNew.get(quad);
			if(setRes != null)
				for(? extends Coordinate obj : setRes)
					setRes.add(obj);
			}
			else
				setRes = setNew;
			
			mapRes.put(quad, setRes);
		}
		return true;
	}
*/
	
	public static boolean AddAll (Map<Integer, HashSet<OpCoord>> mapRes, Map<Integer, HashSet<OpCoord>> mapNew)
	{
		if(mapRes == null || mapNew == null)
			return false;
		
		for(Integer quad : mapNew.keySet())
		{			
			HashSet<OpCoord> setRes = mapRes.get(quad);
			HashSet<OpCoord> setNew = mapNew.get(quad);
			if(setRes != null)
				setRes.addAll(setNew);
			else
				setRes = setNew;
			
			mapRes.put(quad, setRes);
		}
		return true;
	}	
}
