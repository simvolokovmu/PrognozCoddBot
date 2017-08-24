import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;

public class PrognozReport 
{
	public void run() 
    {
		try {
			LoadData();
		} catch (Exception e) {
	    	Core.log_.error(e.getClass().getName() + " : " + e.getMessage());
		}
    }

	private void LoadData()
	{
		try {
//			Quadrant.checkQuad();
//			mapSwap, mapOP = new ConcurrentHashMap<Integer, HashSet<OpCoord>> ();  
			Map<Integer, HashSet<OpCoord>> mapOpByQuad = new ConcurrentHashMap<Integer, HashSet<OpCoord>> (); 
			Map<String, OpCoord> mapOpById = new ConcurrentHashMap<String, OpCoord> (); 
			
			ProcCoorOP(mapOpByQuad, mapOpById, true); // Core.dsTlg_.mapOP_, true);
			ProcCoorOP(mapOpByQuad, mapOpById, false); // Core.dsTlg_.mapOP_, false);
			
			Map<Integer, HashSet<OpCoord>> mapSwapQuad = Core.dsTlg_.mapOpByQuad_; 
			Core.dsTlg_.mapOpByQuad_ = mapOpByQuad;
			mapSwapQuad.clear();

			Map<String, OpCoord> mapSwapId = Core.dsTlg_.mapOPByStopID_; 
			Core.dsTlg_.mapOPByStopID_ = mapOpById;
			mapSwapId.clear();
			
			Core.log_.info("Lat from " + Quadrant.dLatLoadMin_ + " to " + Quadrant.dLatLoadMax_);
			Core.log_.info("Lon from " + Quadrant.dLonLoadMin_ + " to " + Quadrant.dLonLoadMax_);
			
/* GKU OP - list of all stop_bus
			{
				String sOpCoordsCsv = "Широта;Долгота;Описание;Подпись\n" + GetPrintMapCsv(Core.dsTlg_.mapOP_, 0, 100, 0, 100);
				Utils.save2File("./opcoords_comerce_" + (new Long(System.currentTimeMillis()).toString()) + ".csv", sOpCoordsCsv.replace("\\", "").replace("\"",""));//GetPrintMap(Core.dsTlg_.mapOP_));
				
//				String sOpCoordsCsv = "Широта;Долгота;Описание;Подпись\n" + GetPrintMapCsv(Core.dsTlg_.mapOP_, 0, 55.7, 0, 37.5);
//				Utils.save2File("./opcoords_1q_" + (new Long(System.currentTimeMillis()).toString()) + ".csv", sOpCoordsCsv.replace("\\", "").replace("\"",""));//GetPrintMap(Core.dsTlg_.mapOP_));
			}
/*			
			{
				String sOpCoordsCsv = "Широта;Долгота;Описание;Подпись\n" + GetPrintMapCsv(Core.dsTlg_.mapOP_, 55.7, 100, 0, 37.5);
				Utils.save2File("./opcoords_2q_" + (new Long(System.currentTimeMillis()).toString()) + ".csv", sOpCoordsCsv.replace("\\", "").replace("\"",""));//GetPrintMap(Core.dsTlg_.mapOP_));
			}
			{
				String sOpCoordsCsv = "Широта;Долгота;Описание;Подпись\n" + GetPrintMapCsv(Core.dsTlg_.mapOP_, 0, 55.7, 37.5, 100);
				Utils.save2File("./opcoords_3q_" + (new Long(System.currentTimeMillis()).toString()) + ".csv", sOpCoordsCsv.replace("\\", "").replace("\"",""));//GetPrintMap(Core.dsTlg_.mapOP_));
			}
			{
				String sOpCoordsCsv = "Широта;Долгота;Описание;Подпись\n" + GetPrintMapCsv(Core.dsTlg_.mapOP_, 55.7, 100, 37.5, 100);
				Utils.save2File("./opcoords_4q_" + (new Long(System.currentTimeMillis()).toString()) + ".csv", sOpCoordsCsv.replace("\\", "").replace("\"",""));//GetPrintMap(Core.dsTlg_.mapOP_));
			}
*/			
		} catch (Exception e)
		{
			Core.log_.error(e);
		}
	}

	public String GetPrintMap(Map<Integer, HashSet<OpCoord>> mapOP)
	{
		Core.log_.info("PrintMap start");
		String sRes = "";
		for(Integer nq : mapOP.keySet())
		{
			sRes += "Quad:" + nq;
			HashSet<OpCoord> set = mapOP.get(nq);
			int n = 0;
			for(OpCoord op : set)
				sRes += " N:" + (n++) + " Quad:" + nq + " info: " + op.getInfo() + "\n";
		}
		Core.log_.info("PrintMap finish");
		return sRes;
	}

	public String GetPrintMapCsv(Map<Integer, HashSet<OpCoord>> mapOP, double latBefore, double latAfter, double lonBefore, double lonAfter)
	{
		Core.log_.info("PrintMapCsv start");
		String sRes = "";
		for(Integer nq : mapOP.keySet())
		{
			HashSet<OpCoord> set = mapOP.get(nq);
			for(OpCoord op : set)
				if ((latBefore == 0 && latAfter == 0 || latBefore <= op.dLat_ && op.dLat_ < latAfter) &&
					(lonBefore == 0 && lonAfter == 0 || lonBefore <= op.dLon_ && op.dLon_ < lonAfter))
					sRes += op.getInfoCsv() + "\n";
				else
					sRes += "";
			
		}
		Core.log_.info("PrintMapCsv finish");
		return sRes;
	}
	
	private boolean ProcCoorOP(Map<Integer, HashSet<OpCoord>> mapOpByQuad, Map<String, OpCoord> mapOpById, boolean forCommerce)
	{
		String urlCoorOp = ConfigLocal.httpPrefix_ + "/reportdata/getStops/json?group=" + (forCommerce ? "1" : "0");
		String jsonResp = Utils.getAnswerForRequest (urlCoorOp, ConfigLocal.userName_, ConfigLocal.password_); 

		if(jsonResp == null)
		{
			Core.log_.error("OP is empty");
			return false;
		}
		
		return OpCoord.LoadCoorOP(jsonResp, mapOpByQuad, mapOpById);
	}

	public static String GetReportOP(Float lat, Float lon)
	{
		try {
//			GetPrognoz();

			Coordinate coorCur = new Coordinate(lat, lon);
			Core.log_.info("GetReport for coor: " + coorCur.getCoordInfo());
			
			ArrayDeque<Integer> arrQuad = Quadrant.getNearQuad(coorCur.nQuad_);
			String sRes = new String();
			Set<OpCoord> setOPRes = new HashSet<OpCoord>(); 
			
			for(Integer quad : arrQuad)
			{
				Set<OpCoord> setOP = Core.dsTlg_.mapOpByQuad_.get(quad);
				if(setOP == null)
					continue;
				setOPRes.addAll(setOP);
			}
			
			LinkedList<AbstractMap.SimpleEntry<Double, ? extends Coordinate>> lsSort = Coordinate.getSortListByLen2Power(coorCur, setOPRes);
			for(AbstractMap.SimpleEntry<Double, ? extends Coordinate> item : lsSort)
			{
				OpCoord op = (OpCoord)item.getValue();
				sRes += GetReport(op.opName_);
				break;
//				sRes += String.format("%.3fm : ", (Math.sqrt(item.getKey()) / Quadrant.dOneMeter_));
//				sRes += op.opName_ + ' ' + op.opStopId_ + '\n'; 
			}

			if(!sRes.isEmpty())
				return sRes;
		} catch(Exception e)
		{
			Core.log_.error(e);
		}
		return "Остановки не найдены";
	}	
	
	public static String GetReport(String sStopName)
	{
		String rpt = (String)"Прогноз прибытия на остановку\n";
		rpt += "<b>" + sStopName + "</b>\n";
		rpt += ConfigLocal.tableHeaderTrips_;

		switch((int)(System.currentTimeMillis() % 3))
		{
		case 1:
			rpt += "<pre>" + Utils.getFormatString("45", 4, false) + Utils.getFormatString("Троллей", 7, false) + ' ' + Utils.getFormatString("7 мин", 6, true) + "</pre>\n";
			rpt += "<pre>" + Utils.getFormatString("М2", 4, false) + Utils.getFormatString("Автобус", 7, false) + ' ' + Utils.getFormatString("14 мин", 6, true) + "</pre>\n";
			rpt += "<pre>" + Utils.getFormatString("818", 4, false) + Utils.getFormatString("Автобус", 7, false) + ' ' + Utils.getFormatString("2 мин", 6, true) + "</pre>\n";
			break;
		case 2:
			rpt += "<pre>" + Utils.getFormatString("83", 4, false) + Utils.getFormatString("Автобус", 7, false) + ' ' + Utils.getFormatString("3 мин", 6, true) + "</pre>\n";
			rpt += "<pre>" + Utils.getFormatString("603", 4, false) + Utils.getFormatString("Автобус", 7, false) + ' ' + Utils.getFormatString("1 мин", 6, true) + "</pre>\n";
			rpt += "<pre>" + Utils.getFormatString("7", 4, false) + Utils.getFormatString("Троллей", 7, false) + ' ' + Utils.getFormatString("23 мин", 6, true) + "</pre>\n";
			rpt += "<pre>" + Utils.getFormatString("43", 4, false) + Utils.getFormatString("Трамвай", 7, false) + ' ' + Utils.getFormatString("0 мин", 6, true) + "</pre>\n";
			break;
		default :
			rpt += "<pre>" + Utils.getFormatString("903", 4, false) + Utils.getFormatString("Автобус", 7, false) + ' ' + Utils.getFormatString("9 мин", 6, true) + "</pre>\n";
			rpt += "<pre>" + Utils.getFormatString("31", 4, false) + Utils.getFormatString("Трамвай", 7, false) + ' ' + Utils.getFormatString("0 мин", 6, true) + "</pre>\n";
			rpt += "<pre>" + Utils.getFormatString("716", 4, false) + Utils.getFormatString("Автобус", 7, false) + ' ' + Utils.getFormatString("2 мин", 6, true) + "</pre>\n";
			break;
		}
		
		return rpt;
	}

	public static void GetPrognoz()
	{
		String json = "[{\"stop_id\":\" op_947\",\"name\":\"Ул. Ивана Сусанина\", \"trip_id\": \"4150001_1\",\"gosnum\":\"ХУ86277\",\"time\":\"05:30\"},{\"stop_id\":\"op_947\",\"name\":\"Ул. Ивана Сусанина\", \"trip_id\": \"14150022_1700379\",\"gosnum\":\"ХУ86277\",\"time\":\"05:45\"},{\"stop_id\":\"op_947\",\"name\":\"Ул. Ивана Сусанина \",\"trip_id\": \"14150034_1700106\",\"gosnum\":\"ХУ84977\",\"time\":\"05:53\"}]";
		parsePrognozJson(json);
	}
	
	public static void parsePrognozJson(String json)
	{
		JSONArray jsonArray = new JSONArray(json);

		Core.log_.info("parsePrognozJson");
		for (int i = 0; i < jsonArray.length(); i++)
		{
			String stopId = jsonArray.getJSONObject(i).getString("stop_id");
			String name = jsonArray.getJSONObject(i).getString("name");
			String tripId = jsonArray.getJSONObject(i).getString("trip_id");
			String gosNum = jsonArray.getJSONObject(i).getString("gosnum");
			String time = jsonArray.getJSONObject(i).getString("time");
			Core.log_.info(stopId);
			Core.log_.info(name);
			Core.log_.info(tripId);
			Core.log_.info(gosNum);
			Core.log_.info(time);
		}		
	}
}
