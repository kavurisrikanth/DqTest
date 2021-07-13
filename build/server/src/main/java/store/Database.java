package store;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Database {
    private static Database instance;
    @Autowired private EntityMutator mutator;

    @PostConstruct
    public void init() {
        instance = this;
    }

    public static Database get() {
        return instance;
    }

    public void save(Object obj) {
        if (!(obj instanceof DatabaseObject)) {
            return;
        }
        mutator.save((DatabaseObject) obj, true);
    }

    public void saveAll(List<Object> objects) {
        objects.forEach(obj -> save(obj));
    }

    public void update(Object obj) {
        if (!(obj instanceof DatabaseObject)) {
            return;
        }
        mutator.update((DatabaseObject) obj, true);
    }

    public void updateAll(List<Object> objects) {
        objects.forEach(obj -> update(obj));
    }

    public void delete(Object obj) {
        if (!(obj instanceof DatabaseObject)) {
            return;
        }
        mutator.delete((DatabaseObject) obj, true);
    }

    public void deleteAll(List<Object> objects) {
        objects.forEach(obj -> delete(obj));
    }
    
    public void preUpdate(DatabaseObject obj) {
    	mutator.preUpdate(obj);
    }
    
    public void preDelete(DatabaseObject obj) {
        mutator.preDelete(obj);
    }
    
    public static void markDirty(DatabaseObject obj) {
    	get().mutator.markDirty(obj);
    }	

	public static void collectCreatableReferences(List<Object> _refs, DatabaseObject obj) {
		if(obj != null) {
			obj.collectCreatableReferences(_refs);
		}
	}

	public static void collectCollctionCreatableReferences(List<Object> _refs, List<? extends DatabaseObject> coll) {
		coll.forEach(o -> collectCreatableReferences(_refs, o));
	}
}
