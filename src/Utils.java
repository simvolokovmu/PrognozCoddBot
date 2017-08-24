import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class Utils 
{
	public static String getAnswerForRequest(String myURL, String userName, String password)
	{
		String sRptRes = null;
		try {
			URL url = new URL(myURL);
			String nullFragment = null;
			URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), nullFragment);
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(uri); // myURL);
//			httpGet.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(ConfigLocal.userName_, ConfigLocal.password_), "UTF-8", false));
			httpGet.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(userName, password), "UTF-8", false));
		
			HttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity entity = httpResponse.getEntity();
			
			sRptRes = EntityUtils.toString(entity);
		} catch(Exception e)
		{
			Core.log_.info ("Error for request: " + myURL);
			Core.log_.error(e);
		}
		return sRptRes;
	}

	public static String SaveJsonToStore(String json, String sFNPrefix) throws IOException
	{
		byte[] utf8 = json.getBytes("UTF-8"); 
		
		String sFN = sFNPrefix + (new Long(System.currentTimeMillis()).toString()) + ".json";
		String sPFjson = ConfigLocal.dirStore_ + '/' + sFN;
		FileUtils.writeByteArrayToFile(new File(sPFjson), utf8);
		return sPFjson;
	}

	public static String getFormatString(String str, int nFmt, boolean addSpaceToBegining)
	{
		String sDummy = "                    ";
		if(nFmt <= str.length() || nFmt >= sDummy.length())
			return str;
		
		if(addSpaceToBegining)
			return sDummy.substring(0, nFmt - str.length()) + str;
		
		return str + sDummy.substring(0, nFmt - str.length());
	}
	
	public static String getJSonValue(String src, String sKey)
	{
		String sTag = (String)"\"" + sKey + "\":";
		int nIndexStart = src.indexOf(sTag);
		if(nIndexStart == -1)
			return "";
		String sAfter = src.substring(nIndexStart + sTag.length()); // 2 is ":
		
		int nIndexEnd = sAfter.indexOf(",");
		if(nIndexEnd == -1)
			return "";
		String sRes = sAfter.substring(0, nIndexEnd);
//		Core.log_.info(sKey + " : " + sRes);

		return sRes;
	}	

	public static void save2File(String sPFSave, String sContent) throws IOException
	{
		byte[] utf8 = sContent.getBytes("UTF-8"); 
		FileUtils.writeByteArrayToFile(new File(sPFSave), utf8);
	}
}
