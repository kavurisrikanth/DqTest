package d3e.core;

import java.io.IOException;

import javax.servlet.ServletException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionDeligate {

	public static interface ToRun {
		void run() throws ServletException, IOException;
	}

	@Transactional
	public void run(ToRun run) throws ServletException, IOException {
		run.run();
	}

	@Transactional(readOnly = true)
	public void readOnly(ToRun run) throws ServletException, IOException {
		run.run();
	}
}
