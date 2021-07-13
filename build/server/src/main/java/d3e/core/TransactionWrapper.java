package d3e.core;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.UnexpectedRollbackException;

@Component
public class TransactionWrapper {

	@Autowired
	private D3ESubscription subscription;

	@Autowired
	private TransactionDeligate deligate;
	
	@Autowired
	private EntityManager em;

	public void doInTransaction(TransactionDeligate.ToRun run) throws ServletException, IOException {
		boolean created = createTransactionManager();
		try {
			deligate.run(run);
			if (created) {
				publishEvents();
			}
		} catch (UnexpectedRollbackException e) {
			D3ELogger.info("Transaction failed");
		} catch (Exception e) {
			D3ELogger.printStackTrace(e);
			throw new RuntimeException(e);
		} finally {
			if (created) {
				TransactionManager.remove();
			}
		}
	}

	private void publishEvents() throws ServletException, IOException {
		deligate.readOnly(() -> {
			TransactionManager manager = TransactionManager.get();
			TransactionManager.remove();
			createTransactionManager();
			manager.commit(event -> {
				try {
					subscription.handleContextStart(event);
				} catch (Exception e) {
				  	e.printStackTrace();
				}
			});
		});
	}

	private boolean createTransactionManager() {
		TransactionManager manager = TransactionManager.get();
		if (manager == null) {
			manager = new TransactionManager();
			TransactionManager.set(manager);
			return true;
		}
		return false;
	}
}
