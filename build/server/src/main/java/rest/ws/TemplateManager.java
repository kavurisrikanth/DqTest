package rest.ws;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class TemplateManager {

	private Map<String, Template> templates = new HashMap<>();
	private Map<String, Template> predefinedTemplates = new HashMap<>();

	public boolean hasTemplate(String templateId) {
		return predefinedTemplates.containsKey(templateId);
	}

	public Template createTemplate(String templateId) {
		if (!templates.containsKey(templateId)) {
			Template template = new Template(templateId);
			templates.put(templateId, template);
			return template;
		}
		return templates.get(templateId);
	}

	public Set<String> getUsageKeys(String templateId) {
		Template template = templates.get(templateId);
		return template.usages.keySet();
	}

	public Template getTemplate(String templateId) {
		return templates.get(templateId);
	}

	public static String getMd5(String input) {
		try {
			// Static getInstance method is called with hashing MD5
			MessageDigest md = MessageDigest.getInstance("MD5");

			// digest() method is called to calculate message digest
			// of an input digest() return array of byte
			byte[] messageDigest = md.digest(input.getBytes());

			// Convert byte array into signum representation
			BigInteger no = new BigInteger(1, messageDigest);

			// Convert message digest into hex value
			String hashtext = no.toString(16);
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		}
		// For specifying wrong message digest algorithms
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
