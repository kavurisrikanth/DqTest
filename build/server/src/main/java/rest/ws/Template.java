package rest.ws;

import java.util.HashMap;
import java.util.Map;

import graphql.language.Field;

public class Template {

	private String hash;
	private TemplateType[] types;

	public Template(String templateHash) {
		// TODO Auto-generated constructor stub
	}

	public Map<String, Field> usages = new HashMap<>();

	public String getQueryName(int queryNum) {
		// TODO Auto-generated method stub
		return null;
	}

	public UsageType getUsageType(int idx) {
		// TODO Auto-generated method stub
		return null;
	}

	public Field createUsageField(String usage) {
		// TODO Auto-generated method stub
		return null;
	}

	public TemplateType getTypeIndex(String typeHash) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addTypeIndex(int idx) {
		// TODO Auto-generated method stub

	}

	public int getUsageIndex(String usageHash) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setTypeTemplate(int idx, TemplateType tt) {
		// TODO Auto-generated method stub

	}

	public void setUsageTemplate(int idx, UsageType ut) {
		// TODO Auto-generated method stub

	}
}
