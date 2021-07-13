package rest.ws;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gqltosql.schema.DField;
import gqltosql.schema.DModel;
import gqltosql.schema.IModelSchema;

@Service
public class MasterTemplate {

	@Autowired
	private IModelSchema schema;

	private Map<String, TemplateType> typesByHash = new HashMap<>();
	private Map<String, UsageType> usageByHash = new HashMap<>();

	@PostConstruct
	public void init() {
		List<DModel<?>> allTypes = schema.getAllTypes();
		allTypes.forEach(t -> addTemplateType(t));
	}

	private void addTemplateType(DModel<?> md) {
		List<DField<?, ?>> allFields = md.getAllFields();
		allFields.sort((a, b) -> a.getName().compareTo(b.getName()));
		TemplateType tt = new TemplateType(md, allFields.size());
		int i = 0;
		for (DField<?, ?> f : allFields) {
			tt.addField(i++, f);
		}
		typesByHash.put(null, tt);
	}

	public TemplateType getTemplateType(String typeHash) {
		return typesByHash.get(typeHash);
	}

	public UsageType getUsageTemplate(String usageHash) {
		return usageByHash.get(usageHash);
	}
}
