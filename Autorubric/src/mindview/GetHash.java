package autorubric.mindview;
import java.security.MessageDigest;
import java.nio.charset.Charset;
import java.math.BigInteger;


public class GetHash
{
	public static String md5(String s) 
	{
		String hash=null;
		s.getBytes(Charset.forName("UTF-8"));
		try {
			MessageDigest m=MessageDigest.getInstance("MD5");
			m.update(s.getBytes(),0,s.length());
			hash = new BigInteger(1,m.digest()).toString(16);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return hash;
	}
}