package rest.ws;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import d3e.core.D3ELogger;
import d3e.core.TransactionWrapper;
import gqltosql.schema.DField;
import gqltosql.schema.DModel;
import gqltosql.schema.IModelSchema;
import store.DatabaseObject;
import store.EntityHelperService;
import store.ValidationFailedException;

@Configuration
@EnableWebSocket
public class D3EWebsocket extends BinaryWebSocketHandler implements WebSocketConfigurer {

	private static final int ERROR = 0;
	private static final int CONFIRM_TEMPLATE = 1;
	private static final int HASH_CHECK = 2;
	private static final int TYPE_EXCHANGE = 3;
	private static final int RESTORE = 4;
	private static final int QUERY = 5;
	private static final int SAVE = 6;
	private static final int DELETE = 7;
	private static final int UNSUBSCRIBE = 8;
	private static final int CHANNEL_MESSAGE = 9;

	@Autowired
	private TemplateManager templateManager;

	@Autowired
	private TransactionWrapper wrapper;

	@Autowired
	private ObjectFactory<EntityHelperService> helperService;

	@Autowired
	private Map<String, SocketChannel> allChannels;

	@Autowired
	private RocketQuery query;

	@Autowired
	private RocketMutation mutation;

	@Autowired
	private IModelSchema schema;

	@Autowired
	private MasterTemplate master;

	@Value("${rocket.reconnectPeriode:300}")
	private int reconnectPeriode;

	private Map<String, ClientSession> sessions = new HashMap<>();
	private Map<String, ClientSession> disconnectedSessions;

	@PostConstruct
	public void init() {
		disconnectedSessions = new HashMap<>(); // TODO new
		// MapMaker().concurrencyLevel(4).weakValues().expiration(reconnectPeriode,
		// TimeUnit.SECONDS);
	}

	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(this).setAllowedOrigins("*");
	}

	@Override
	public boolean supportsPartialMessages() {
		return true;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		D3ELogger.info("D3EWebsocket connected. " + session.getId());
		sessions.put(session.getId(), new ClientSession(session));
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		D3ELogger.info("D3EWebsocket connection closed. " + session.getId());
		ClientSession remove = sessions.remove(session.getId());
		remove.session = null;
		disconnectedSessions.put(remove.getId(), remove);
	}

	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
		ClientSession cs = sessions.get(session.getId());
		ByteBuffer payload = message.getPayload();
		cs.stream.writeBytes(payload.array());
		if (!message.isLast()) {
			return;
		}
		RocketMessage reader = new RocketMessage(null, cs.stream);
		cs.stream = new ByteArrayOutputStream();
		try {
			wrapper.doInTransaction(() -> {
				onMessage(cs, reader);
				cs.session.sendMessage(new BinaryMessage(reader.getOutput()));
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void onMessage(ClientSession ses, RocketMessage msg) {
		msg.writeInt(msg.readInt());
		int type = msg.readByte();
		switch (type) {
		case CONFIRM_TEMPLATE:
			onConfirmTemplate(ses, msg);
			break;
		case HASH_CHECK:
			onHashCheck(ses, msg);
			break;
		case TYPE_EXCHANGE:
			onTypeExchange(ses, msg);
			break;
		case RESTORE:
			onRestore(ses, msg);
		case QUERY:
			onQuery(ses, msg);
			break;
		case SAVE:
			onSave(ses, msg);
			break;
		case DELETE:
			onDelete(ses, msg);
			break;
		case UNSUBSCRIBE:
			onUnsubscribe(ses, msg);
			break;
		case CHANNEL_MESSAGE:
			onChannelMessage(ses, msg);
			break;
		default:
			msg.writeByte(ERROR);
			msg.writeString("Unsupported type: " + type);
		}
	}

	private void onChannelMessage(ClientSession ses, RocketMessage msg) {
		msg.writeByte(CHANNEL_MESSAGE);
		String channelName = msg.readString();
		SocketChannel channel = allChannels.get(channelName);
		if (channel == null) {
			msg.writeByte(1);
		} else {
			try {
				msg.writeByte(0);
				channel._message(ses, msg);
			} catch (Exception e) {
				msg.writeByte(1);
				msg.writeString(e.getMessage());
			}
		}
	}

	private void onUnsubscribe(ClientSession ses, RocketMessage msg) {
		msg.writeByte(UNSUBSCRIBE);
		String subId = msg.readString();
		// TODO Subscription Id

	}

	private void onDelete(ClientSession ses, RocketMessage msg) {
		msg.writeByte(DELETE);
		String type = msg.readType();
		long id = msg.readLong();
		try {
			mutation.delete(type, id);
			msg.writeByte(0);
		} catch (ValidationFailedException e) {
			// TODO: handle exception
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void onSave(ClientSession ses, RocketMessage msg) {
		msg.writeByte(SAVE);
		DatabaseObject obj = (DatabaseObject) msg.readObject();
		try {
			mutation.save(obj.getClass().getSimpleName(), obj);
			Map<Long, Long> localIds = new HashMap<>();
			obj.updateMasters(a -> {
				if (a.localId != 0l) {
					localIds.put(a.localId, a.getId());
				}
			});
			msg.writeByte(0);
			msg.writeInt(localIds.size());
			localIds.forEach((k, v) -> {
				msg.writeLong(k);
				msg.writeLong(v);
			});
			// TODO write all data back
		} catch (ValidationFailedException e) {
			// TODO: handle exception
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void onQuery(ClientSession ses, RocketMessage msg) {
		msg.writeByte(QUERY);
		int queryNum = msg.readShort();
		String queryName = ses.template.getQueryName(queryNum);
		int usageId = msg.readShort();
		UsageType usage = ses.template.getUsageType(usageId);
		boolean withSubscription = msg.readBoolean();
		// TODO set current user from ClientSession
		// TODO discuss about UserSession.
		RocketInputContext ctx = new RocketInputContext(helperService.getObject(), msg);
		try {
			QueryResult queryRes = query.executeOperation(queryName, null, ctx);
			if (queryRes.external) {
				new RocketObjectDataFetcher(schema, msg).fetch(usage, queryRes.value);
			} else {
				new RocketOutObjectFetcher(schema, msg).fetch(usage, queryRes.value);
			}
			// TODO get user from CurrentUser and save it in ClientSession
			// TODO send Subscription Id for `withSubscription` queries.
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void onRestore(ClientSession ses, RocketMessage msg) {
		msg.writeByte(RESTORE);
		String sessionId = msg.readString();
		if (sessions.containsKey(sessionId)) {
			msg.writeByte(0);
		} else {
			ClientSession disSes = disconnectedSessions.remove(sessionId);
			if (disSes == null) {
				msg.writeByte(1);
			} else {
				disSes.session = ses.session;
				msg.writeByte(0);
				sessions.put(disSes.getId(), disSes);
			}
		}
	}

	private void onConfirmTemplate(ClientSession ses, RocketMessage msg) {
		msg.writeByte(CONFIRM_TEMPLATE);
		msg.writeString(ses.getId());
		String templateHash = msg.readString();
		if (templateManager.hasTemplate(templateHash)) {
			ses.template = templateManager.getTemplate(templateHash);
			msg.writeByte(0);
		} else {
			msg.writeByte(1);
		}
	}

	private void onHashCheck(ClientSession ses, RocketMessage msg) {
		msg.writeByte(HASH_CHECK);
		ses.template = new Template(null);
		// Types
		int types = msg.readInt();
		List<Integer> unknownTypes = new ArrayList<>();
		for (int i = 0; i < types; i++) {
			String typeHash = msg.readString();
			TemplateType tt = master.getTemplateType(typeHash);
			ses.template.setTypeTemplate(i, tt);
			if (tt == null) {
				unknownTypes.add(i);
			}
		}

		// Usages
		int usages = msg.readInt();
		List<Integer> unknownUsages = new ArrayList<>();
		for (int i = 0; i < usages; i++) {
			String usageHash = msg.readString();
			UsageType ut = master.getUsageTemplate(usageHash);
			ses.template.setUsageTemplate(i, ut);
			if (ut == null) {
				unknownUsages.add(i);
			}
		}

		if (unknownTypes.isEmpty() && unknownUsages.isEmpty()) {
			computeTemplateMD5AndAddToManager();
		}
		msg.writeIntegerList(unknownTypes);
		msg.writeIntegerList(unknownUsages);
	}

	private void computeTemplateMD5AndAddToManager() {
		// TODO compute md5 and add to the templateManager

	}

	private void onTypeExchange(ClientSession ses, RocketMessage msg) {
		msg.writeByte(TYPE_EXCHANGE);
		Template template = ses.template;
		// Types
		int typesCount = msg.readInt();
		for (int i = 0; i < typesCount; i++) {
			int idx = msg.readInt();
			String type = msg.readString();
			DModel<?> md = schema.getType(type);
			if (md != null) {
				int fieldsCount = msg.readInt();
				TemplateType tt = new TemplateType(md, fieldsCount);
				template.setTypeTemplate(idx, tt);
				for (int j = 0; j < fieldsCount; j++) {
					String field = msg.readString();
					int typeIdx = msg.readInt();
					// TODO check type
					DField<?, ?> df = md.getField(field);
					if (df != null) {
						tt.addField(j, df);
					} else {
						// TODO unknown field
					}
				}
			} else {
				// TODO unknown type
			}
		}

		// Usage
		int usageCount = msg.readInt();
		for (int i = 0; i < usageCount; i++) {
			int idx = msg.readInt();
			UsageType ut = createUsageType(msg);
			template.setUsageTemplate(idx, ut);
		}
		computeTemplateMD5AndAddToManager();
	}

	private UsageType createUsageType(RocketMessage msg) {
		int typeIdx = msg.readInt();
		int fieldsCount = msg.readInt();
		UsageType ut = new UsageType(typeIdx, fieldsCount);
		for (int j = 0; j < fieldsCount; j++) {
			// TODO Auto-generated method stub
		}
		return ut;
	}
}
