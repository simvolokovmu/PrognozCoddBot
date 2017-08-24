import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

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
import org.telegram.telegrambots.ApiContextInitializer;
// import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
 
public class PrognozCoddBot extends TelegramLongPollingBot 
{
	private boolean bWaitEnterDay_ = false;
	private boolean bWaitEnterYstd_ = false;
	private boolean bWaitEnterWeek_ = false;
	
	public static void main(String[] args) 
	{
		
		ApiContextInitializer.init();
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
		try {
        	Core.initialize();
        	Core.Run();
        	
        	
			telegramBotsApi.registerBot(new PrognozCoddBot());
//		} catch (TelegramApiException e) {
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public String getBotUsername() //"»Ãﬂ_œŒÀ‹«Œ¬¿“≈Àﬂ_¬¿ÿ≈√Œ_¡Œ“¿"; 
	{
		switch(ConfigLocal.nDebugMode_)
		{
		case 0: // productive
			return "PrognozCoddBot";
			
		case 1: // test
			return "testprognozcoddbot";
			
		}
		Core.log_.error("Debug mode not found!!");
		return "PrognozCoddBot";
	}
 
	@Override
	public String getBotToken() // "¬¿ÿ_“Œ ≈Õ";
	{
		switch(ConfigLocal.nDebugMode_)
		{
		case 0: // productive: PrognozCodd
			return "423629837:AAHukDM2idlvHjaN_KHijNNdhbOruw-kv3Q";
			
		case 1: // test: PrognozCodd // chat_id -1001145320926 // https://api.telegram.org/bot396864912:AAGI3i-jVVRS9nW3MnN3gu1iiZ_W22pyiPU/sendMessage?chat_id=@testpubliccoddchannel&text=%22hellow12-234234!%22
			return "396864912:AAGI3i-jVVRS9nW3MnN3gu1iiZ_W22pyiPU";
			
		}
		Core.log_.error("Debug mode not found!!");
		return "423629837:AAHukDM2idlvHjaN_KHijNNdhbOruw-kv3Q";
	}

	@Override
	public void onUpdateReceived(Update update) 
	{
		Message message = update.getMessage();
		
		if(message != null && message.hasLocation())
		{
			sendMsg(message, PrognozReport.GetReportOP(message.getLocation().getLatitude(), message.getLocation().getLongitude())); 
		}
		
		if (message == null || !message.hasText())
			return ;

//		String sRes = PrognozReport.GetReportOP(55.782307f, 37.586511f);//55.779583f, 37.58575f);
//		sendMsg(message, sRes); 
		
		String sCmd = message.getText().trim().toLowerCase().replace("  ", " ");
		if (sCmd.equalsIgnoreCase(ConfigLocal.cmdHelp_))
			sendMsg(message, ConfigLocal.descHelp_);
		else if (sCmd.equalsIgnoreCase(ConfigLocal.cmdKill9_))
		{
			sendMsg(message, "Service will be stopped");				
			Core.stop(); // terminate service				
		} else 
			sendMsg(message, ConfigLocal.cmdUknown_ + ConfigLocal.descHelp_);

	}	

	private void sendMsg(Message message, String text) 
	{
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableMarkdown(true);
		sendMessage.setChatId(message.getChatId().toString());
		sendMessage.setReplyToMessageId(message.getMessageId());
		sendMessage.setText(text);
		sendMessage.setParseMode("HTML");
		sendMessage.enableHtml(true);
/*		
		ReplyKeyboardMarkup kb = new ReplyKeyboardMarkup();
		kb.setResizeKeyboard(true);
		List<KeyboardRow> lsKb = new ArrayList<KeyboardRow>();

		if(bWaitEnterDay_)
		{
			bWaitEnterDay_ = false;
			List<KeyboardRow> lsRow = new ArrayList<KeyboardRow>();
			for(int i = 0; i < 7; i++)
				lsRow.add(new KeyboardRow());
				
			long msec = System.currentTimeMillis() + 7*Core.dayInMSec_; // full current week
			for(int week = 0; week < 4; week++)
			{
				int d = 0;
				for(String sDt : Core.GetWeek(msec))
					lsRow.get(d++).add(new KeyboardButton(sDt));

				msec -= 7 * Core.dayInMSec_;
			}
			for(KeyboardRow row : lsRow)
				lsKb.add(row);
			Collections.reverse(lsKb);
		} else if(bWaitEnterWeek_ || bWaitEnterYstd_)
		{
			List<KeyboardRow> lsRow = new ArrayList<KeyboardRow>();
			KeyboardRow row1 = new KeyboardRow();
			row1.add(new KeyboardButton(ConfigLocal.cmdRpdOnTime_));
			row1.add(new KeyboardButton(ConfigLocal.cmdRpdOnTS_));

			KeyboardRow row2 = new KeyboardRow();
			row2.add(new KeyboardButton(ConfigLocal.cmdRpdOnShed_));
			row2.add(new KeyboardButton(ConfigLocal.cmdRpdOnTrips_));
				
			lsKb.add(row1);
			lsKb.add(row2);
		} else {
			List<KeyboardRow> lsRow = new ArrayList<KeyboardRow>();
			KeyboardRow row1 = new KeyboardRow();
			row1.add(new KeyboardButton(ConfigLocal.cmdRpdWeek_));
			row1.add(new KeyboardButton(ConfigLocal.cmdRpdYstdTS_));

			KeyboardRow row2 = new KeyboardRow();
			row2.add(new KeyboardButton(ConfigLocal.cmdRpdDay_));
			row2.add(new KeyboardButton(ConfigLocal.cmdRpdYstdTrips_));
				
			lsKb.add(row1);
			lsKb.add(row2);
		}
		kb.setKeyboard(lsKb);
		sendMessage.setReplyMarkup(kb);
*/		
		try {
			sendMessage(sendMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}