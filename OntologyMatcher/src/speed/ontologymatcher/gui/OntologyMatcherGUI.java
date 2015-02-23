package speed.ontologymatcher.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import speed.ontologymatcher.gui.panels.MainPanel;

import com.jgoodies.looks.windows.WindowsLookAndFeel;

public class OntologyMatcherGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JPanel jContentPane = null;

	/**
	 * This is the default constructor
	 */
	public OntologyMatcherGUI() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		try {
			UIManager.setLookAndFeel(new WindowsLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			System.err.println("Interface windows n�o carregada.");
		}
		this.setSize(800, 700);
		this.setResizable(false);
		this.setContentPane(getJContentPane());
		this.setTitle("SPEED");
		this.setContentPane(new MainPanel());
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
		}
		return jContentPane;
	}

}
