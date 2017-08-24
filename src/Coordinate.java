import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeMap;

public class Coordinate 
{
	public final Double dLat_;
	public final Double dLon_;
	public final Integer nQuad_;
	
	public Coordinate(double dLat, double dLon) throws Exception
	{
		nQuad_ = Quadrant.getQuadrant(dLat, dLon);
		dLat_ = dLat;
		dLon_ = dLon;
	}

	public String getCoordInfo()
	{
		return (String)"Lat:" + dLat_ + " Lon:" + dLon_ + " Quad:" + nQuad_;
	}
	
	public boolean isEquiv(Coordinate coor)
	{
		return nQuad_ == coor.nQuad_ && dLat_ == coor.dLat_ && dLon_ == coor.dLon_; 
	}
	
	public static boolean isNear(Coordinate coor1, Coordinate coor2)
	{
		if(coor1.nQuad_ == coor2.nQuad_)
			return true;
		
		if(Math.abs(coor1.dLon_ - coor2.dLon_) <= Quadrant.getLonQuadLen() && 
				Math.abs(coor1.dLat_ - coor2.dLat_) <= Quadrant.getLatQuadLen())
			return true;
		
		return false;
	}
	
	public static double getLen2Power(Coordinate coor1, Coordinate coor2) // more fast for find nearest point
	{
		double lenLon = coor1.dLon_ - coor2.dLon_;
		double lenLat = coor1.dLat_ - coor2.dLat_;
		return lenLon * lenLon + lenLat * lenLat;		
	}
	
	public static double getLen(Coordinate coor1, Coordinate coor2)
	{
		double lenLon = coor1.dLon_ - coor2.dLon_;
		double lenLat = coor1.dLat_ - coor2.dLat_;
		return Math.sqrt(lenLon * lenLon + lenLat * lenLat);
	}
	
	public static Coordinate getNearest(Coordinate coorSrc, Set<Coordinate> set)
	{
		double len2PRes = -1.;
		Coordinate coorRes = null;
		for(Coordinate coorCur : set)
		{
			double len2P = getLen2Power(coorSrc, coorCur);
			if(len2PRes <= len2P)
				continue;
			len2PRes = len2P;
			coorRes = coorCur;
		}
		return coorRes;
	}

	public static String getInfoList(LinkedList<AbstractMap.SimpleEntry<Double, ? extends Coordinate>> lsSort)
	{
		String sRes = new String();
		for(AbstractMap.SimpleEntry<Double, ? extends Coordinate> item : lsSort)
		{
			OpCoord op = (OpCoord)item.getValue();
			sRes += String.format("%.3fm : ", (Math.sqrt(item.getKey()) / Quadrant.dOneMeter_));
			sRes += op.opName_ + ' ' + op.opStopId_ + " https://yandex.ru/maps/?mode=search&text=" + op.dLat_ + "%20" + op.dLon_ + "&z=15\n"; 
		}
		return sRes;
	}

	public static LinkedList<AbstractMap.SimpleEntry<Double, ? extends Coordinate>> getSortListByLen2Power(Coordinate coorSrc, Set<? extends Coordinate> set)
	{
		LinkedList<AbstractMap.SimpleEntry<Double, ? extends Coordinate>> lsRes = new LinkedList<AbstractMap.SimpleEntry<Double, ? extends Coordinate>> (); 
		for(Coordinate coorCur : set)
		{
			double len2Power = getLen2Power(coorSrc, coorCur);
	        Core.log_.info("len2pow = " + len2Power);
	        
			ListIterator<AbstractMap.SimpleEntry<Double, ? extends Coordinate>> it = lsRes.listIterator();
			while (it.hasNext()) 
			{
				if(it.next().getKey() > len2Power)
				{
					it.previous();
					it.add(new AbstractMap.SimpleEntry<Double, Coordinate> (len2Power, coorCur));
					Core.log_.info("break");
					break;
				}
			}
			if (!it.hasNext())
				lsRes.add(new AbstractMap.SimpleEntry<Double, Coordinate> (len2Power, coorCur));
			
	        Core.log_.info(getInfoList(lsRes));
		}
        Core.log_.info("finish");
        Core.log_.info(getInfoList(lsRes));
		return lsRes;
	}
}
