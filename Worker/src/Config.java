package myworker;
import java.util.*;

public class Config
{
	private static final String yandexKey="yandex key";
	private static final String yandexURL="https://api.content.market.yandex.ru";
	private static final String yandexVersion="v1";
	private static final String defaultGeoId="157";
	private static final String dbHost="localhost";
	private static final String dbPort="3306";
	private static final String dbUser="root";
	private static final String dbPassword="root";
	private static final String dbName="myworker";
	public static HashMap<String,String> getYandexConfig()
	{
		HashMap<String,String> yandexConfig=new HashMap<String,String>();
		yandexConfig.put("key",yandexKey);
		yandexConfig.put("url",yandexURL);
		yandexConfig.put("version",yandexVersion);
		yandexConfig.put("defaultGeoId",defaultGeoId);
		return yandexConfig;
	}
	public static HashMap<String,String> getDBConfig()
	{
		HashMap<String,String> dbConfig=new HashMap<String,String>();
		dbConfig.put("host",dbHost);
		dbConfig.put("port",dbPort);
		dbConfig.put("user",dbUser);
		dbConfig.put("password",dbPassword);
		dbConfig.put("name",dbName);
		return dbConfig;
	}
}