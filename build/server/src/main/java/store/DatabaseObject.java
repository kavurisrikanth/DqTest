package store;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;

import d3e.core.CloneContext;

@MappedSuperclass
public abstract class DatabaseObject  implements  DBObject, ICloneable {

	@javax.persistence.Id
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
	protected long id;

	public transient long localId;
	public transient boolean isOld;

	public long getId() {
		return this.id;
	}

	public void setIsOld(boolean isOld) {
		this.isOld = isOld;
	}
	
	public void setId(long id) {
		this.id = id;
		if(localId == 0) {
			localId = id;
		}
	}

	private static Logger logger = Logger.getLogger("DatabaseObject");
	private transient static int MAX_SAVE_COUNT = 20;
	private transient static int OBJ_LOG_COUNT = 5;
	private transient boolean isDeleted;
	public transient boolean isImportingFromXml;
	private transient boolean isDirty;
	private transient boolean isNew = true;
	private transient boolean needsUpdate;
	private transient boolean isDeleteProcessed;
	protected transient boolean needSaveProcess = true;
	protected transient boolean needDeleteProcess = true;
	private transient int onSaveCount;
	private transient boolean isGotId;
	protected DBSaveStatus saveStatus = DBSaveStatus.New;
	private transient boolean isInConvert;
	private transient boolean loaded;

	public DatabaseObject() {
	}

	public boolean isDeleted() {
		return this.isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public boolean isInConvert() {
		return this.isInConvert;
	}

	public void setInConvert(boolean isInConvert) {
		this.isInConvert = isInConvert;
	}

	protected Object toLogStr(Collection<?> col) {
		return col.size();
	}

	protected Object toLogStr(DatabaseObject db) {
		if (db == null) {
			return null;
		}
		return db.getId();
	}

	public String getObjectName() {
		return null;
	}

	protected void setInputs() {

	}
	public void updateFlat(DatabaseObject obj) {
		
	}

	public void setNeedDeleteProcess(boolean needDeleteProcess) {
		this.needDeleteProcess = needDeleteProcess;
	}

	protected boolean isComponent() {
		return false;
	}

	public void setNeedSaveProcess(boolean needSaveProcess) {
		this.needSaveProcess = needSaveProcess;
	}

	public void setDeletedProcessed(boolean isDeleteProcessed) {
		this.isDeleteProcessed = isDeleteProcessed;
	}

	public boolean isDeleteProcessed() {
		return isDeleteProcessed;
	}

	public void recordLog() {
	}

	@Override
	public boolean equals(Object a) {
		boolean isEquals = false;
		if ((a instanceof DatabaseObject)) {
			DatabaseObject aObj = (DatabaseObject) a;
			if ((!aObj.isNew() && !this.isNew())) {
				isEquals = (aObj.getId() == this.getId());
			} else {
				isEquals = (aObj == this);
			}
		}
		return isEquals;
	}

	public DatabaseObject clone() {
		return null;
	}

	public void cloneInstance(DatabaseObject clone) {
		clone.id = this.id;
	}

	public boolean isNew() {
		return !loaded || id == 0;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public void updateMasters(Consumer<DatabaseObject> visitor) {
		updateFlat(this);
	}

	public DatabaseObject _masterObject() {
		return null;
	}
	public boolean transientModel() {
		return false;
	}

	public void collectChildValues(CloneContext ctx) {
		
	}

	public void deepCloneIntoObj(ICloneable cloned, CloneContext ctx) {
		DatabaseObject db = (DatabaseObject) cloned;
		db.setId(id);
	}

	public void recordOld(CloneContext ctx) {
	}

	public DatabaseObject getOld() {
	  return null;
	}

	public long getLocalId(){
	  return localId;
	}

	protected boolean canMarkDirty() {
	  return loaded;
	}

	protected boolean canCreateOldObject() {
	  return needOldObject() && !(isOld || id == 0l || !loaded || getOld() != null);
	}

	protected boolean needOldObject() {
		return false;
	}
	
	public void createOldObject() {
	  CloneContext ctx = new CloneContext(true, true);
	  ctx.startClone(this);
	  recordOld(ctx);
	}

	@PostLoad
	public void postLoad() {
	  loaded = true;
	}

	public boolean _creatable() {
		return false;
	}
	
	public void _markDirty() {
		isDirty = true;
		DatabaseObject master = _masterObject();
		if(master != null) {
			master._markDirty();
		}
	}

	public void _clearDirty() {
		isDirty = false;
	}
	
	public boolean _isDirty() {
		return isDirty;
	}
	
	protected void onPropertySet() {
		_markDirty();
		if(canMarkDirty()) {
			DatabaseObject obj = this;
			do {
				if(obj._creatable()) {
					if(obj.canMarkDirty()) {
						Database.markDirty(this);
					}
					break;
				}
				obj = obj._masterObject();
			} while(obj != null);
		}
		if (canCreateOldObject()) {
			DatabaseObject obj = this;
			do {
				if(obj._creatable()) {
					if(obj.canCreateOldObject()) {
						obj.createOldObject();
					}
					break;
				}
				obj = obj._masterObject();
			} while(obj != null);
		}
	}

	public void collectCreatableReferences(List<Object> _refs) {
	}

	public boolean _isEntity() {
		return false;
	}
}
