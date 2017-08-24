import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class Quadrant 
{
	final public static double dLonMin_ = 36.7; 
	final public static double dLonMax_ = 38.4; // Lon from 36.8637950816 to 38.30948353

	final public static double dLatMin_ = 55.0; 
	final public static double dLatMax_ = 56.2; // Lat from 55.18041692 to 56.1293605 

	final public static int nLonMaxNet_ = 500; 
	final public static int nLatMaxNet_ = 500; 
	
//	final public static double dDeltaPos_ = .001; // 60 metres
	final public static double dOneMeter_ = .0001; // 1 meter in GPS

	public static double dLonLoadMax_ = dLonMin_; // 55.932636;
	public static double dLonLoadMin_ = dLonMax_; // 55.479549;
	public static double dLatLoadMax_ = dLatMin_; // 37.856091;
	public static double dLatLoadMin_ = dLatMax_; // 37.170817;

	public static double getLatQuadLen()
	{
		return (dLatMax_ - dLatMin_) / nLatMaxNet_;
	}
	
	public static double getLonQuadLen()
	{
		return (dLonMax_ - dLonMin_) / nLonMaxNet_;
	}

	public static Integer getQuadrant(double lat, double lon) throws Exception
	{
// !!! Exclude first-last rows-column in net		
		if(lat < dLatMin_ + getLatQuadLen() || dLatMax_ - getLatQuadLen() < lat || 
				lon < dLonMin_ + getLonQuadLen()|| dLonMax_ - getLonQuadLen() < lon) 
			throw new Exception (ConfigLocal.infPosNotInMoscow_ + (String)" (" + lat + ", " + lon + ")");
		
		if(lon > dLonLoadMax_)
			dLonLoadMax_ = lon;
		if(lon < dLonLoadMin_)			
			dLonLoadMin_ = lon;
		if(lat < dLatLoadMin_)			
			dLatLoadMin_ = lat;
		if(lat > dLatLoadMax_)			
			dLatLoadMax_ = lat;
		
		int nLonQuad = (int)((lon - dLonMin_)/(dLonMax_ - dLonMin_) * nLonMaxNet_);
		int nLatQuad = (int)((lat - dLatMin_)/(dLatMax_ - dLatMin_) * nLatMaxNet_) * nLonMaxNet_;
		
		return nLonQuad + nLatQuad;
	}

	public static Coordinate getLatLonCornerForQuad(Integer nQuad) throws Exception
	{
		double dLatQuad = dLatMin_ + ((dLatMax_ - dLatMin_) / nLatMaxNet_) * (nQuad / nLonMaxNet_);
		double dLonQuad = dLonMin_ + ((dLonMax_ - dLonMin_) / nLonMaxNet_) * (nQuad % nLonMaxNet_);

		return new Coordinate(dLatQuad, dLonQuad); 
	}
	
	public static void checkQuad() throws Exception
	{
		String sResCsv = "Широта;Долгота;Описание;Подпись\n";
		
		for(Integer n = 0; n < nLatMaxNet_* nLonMaxNet_; n++)
		{
			Coordinate coor = getLatLonCornerForQuad(n);
			if(n % nLonMaxNet_ < 20 && (100. * n) / nLatMaxNet_ < 30)
			{
				sResCsv += coor.dLat_ + ";";
				sResCsv += coor.dLon_ + ";";
				sResCsv += n + ";" + n + ";\n";
			}
			if(n != coor.nQuad_)
				Core.log_.error("Not correct calc of quadrant " + n + " calc: " + coor.nQuad_);
		}
		Utils.save2File((String)"./quad_all_" + (new Long(System.currentTimeMillis()).toString()) + ".csv", sResCsv);
	}
	
	public static ArrayDeque<Integer> getNearQuad(double lat, double lon) throws Exception
	{
		return getNearQuad (getQuadrant(lat, lon));
	}
	
	public static ArrayDeque<Integer> getNearQuad(int nQuad) throws Exception
	{
		ArrayDeque<Integer> arr= new ArrayDeque<Integer>();
		for (int i = -1; i < 1; i++)
		{
			arr.add(nQuad + i*nLonMaxNet_ - 1);
			arr.add(nQuad + i*nLonMaxNet_);
			arr.add(nQuad + i*nLonMaxNet_ + 1);
		}
		return arr;
	}

}
