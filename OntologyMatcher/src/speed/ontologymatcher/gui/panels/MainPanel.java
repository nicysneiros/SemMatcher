package speed.ontologymatcher.gui.panels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import speed.ontologymatcher.basics.WeightParam;
import speed.ontologymatcher.facade.Facade;
import speed.ontologymatcher.gui.util.OwlFilter;
import speed.ontologymatcher.semanticmatching.basics.Alignment;
import speed.ontologymatcher.semanticmatching.basics.ESSMFunction;
import speed.ontologymatcher.util.sparql.StorageManager;

import java.awt.Font;
import java.awt.Component;
import java.io.File;

import javax.swing.border.TitledBorder;
import javax.swing.SwingConstants;

public class MainPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JButton btnChooseLocalOntology1 = null;
	private JLabel lblLocalOntology1 = null;
	private JTextField txtLocalOntology1 = null;
	private JLabel lblLocalOntology2 = null;
	private JTextField txtLocalOntology2 = null;
	private JButton btnChooseLocalOntology2 = null;
	private JButton btnExecuteMatching = null;
	private JLabel lblDomainOntology = null;
	private JTextField txtDomainOntology = null;
	private JButton btnChooseDomainOntology = null;
	
	private ArrayList<Alignment> matchingFinal;
	//private String finalAlignmentsFile = null;
	
	private JButton btnSave = null;
	private JButton btnSaveToDB = null;
	private JScrollPane pnlScrollableResultMatch = null;
	private JButton btnCalculateSSM = null;
	private JTable tblMatchings = null;
	private JRadioButton rdnDICE = null;
	private JRadioButton rdnAverage = null;
	private JLabel lblMeasure = null;
	private JLabel lblMessageTable = null;
	private JButton btnGenerateAco = null;
	private JLabel lblIs;
	private JLabel lblIssuperconceptof;
	private JLabel lblIs_1;
	private JLabel lblIswholeof;
	private JLabel lblIsdisjointwith;
	private JLabel lblIscloseto;
	private JTextField inputEquivalent;
	private JTextField inputSubConcept;
	private JTextField inputSuperConcept;
	private JTextField inputPart;
	private JTextField inputWhole;
	private JTextField inputDisjoint;
	private JTextField inputClose;
	
	private Facade facade;
	private JPanel panel;
	private JLabel lblLEMatchingWeight;
	private JTextField inputLE;
	private JLabel lblSemanticMatchingWeight;
	private JTextField inputSemantic;
	/**
	 * This is the default constructor
	 */
	public MainPanel() {
		super();
		this.facade = Facade.getInstance();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	@SuppressWarnings("all")
	private void initialize() {
		lblMessageTable = new JLabel();
		lblMessageTable.setBounds(new Rectangle(347, 409, 413, 24));
		lblMessageTable.setText("");
		lblMeasure = new JLabel();
		lblMeasure.setBounds(new Rectangle(447, 543, 161, 30));
		lblMeasure.setText("Choose the function to use:");
		lblDomainOntology = new JLabel();
		lblDomainOntology.setBounds(new Rectangle(32, 161, 178, 27));
		lblDomainOntology.setText("Choose domain ontology:");
		lblLocalOntology2 = new JLabel();
		lblLocalOntology2.setBounds(new Rectangle(32, 89, 167, 23));
		lblLocalOntology2.setText("Choose Ontology 2:");
		lblLocalOntology1 = new JLabel();
		lblLocalOntology1.setBounds(new Rectangle(32, 22, 167, 24));
		lblLocalOntology1.setText("Choose Ontology 1:");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		this.setSize(800, 633);
		this.setLayout(null);
		this.add(getBtnChooseLocalOntology1(), gridBagConstraints);
		this.add(lblLocalOntology1, null);
		this.add(getTxtLocalOntology1(), null);
		this.add(lblLocalOntology2, null);
		this.add(getTxtLocalOntology2(), null);
		this.add(getBtnChooseLocalOntology2(), null);
		this.add(getBtnExecuteMatching(), null);
		this.add(lblDomainOntology, null);
		this.add(getTxtDomainOntology(), null);
		this.add(getBtnChooseDomainOntology(), null);
		this.add(getBtnSave(), null);
		this.add(getBtnSaveToDB(), null);
		this.add(getPnlScrollableResultMatch(), null);
		this.add(getBtnCalculateSSM(), null);
		this.add(getRdnDICE(), null);
		this.add(getRdnAverage(), null);
		this.add(lblMeasure, null);
		this.add(lblMessageTable, null);
		this.add(getBtnGenerateAco(), null);
		
		ButtonGroup bgroup = new ButtonGroup();
		bgroup.add(getRdnDICE());
		bgroup.add(getRdnAverage());
		
		tblMatchings.setModel(
				new DefaultTableModel(
						new Object [][] {
		                    //{null, null, null, null}
		                },
		                new String [] {
		                    "Ontology 1", "Correspondence", "Ontology 2", "Measure"
		                }
	            	) 
				{	
					
	                Class[] types = new Class [] {
	                    java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class
	                };
	                boolean[] canEdit = new boolean [] {
	                    false, false, false, false
	                };

	                public Class getColumnClass(int columnIndex) {
	                    return types [columnIndex];
	                }

	                public boolean isCellEditable(int rowIndex, int columnIndex) {
	                    return canEdit [columnIndex];
	                }
	            });
		add(getPanel());
	}

	/**
	 * This method initializes btnChooseLocalOntology1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtnChooseLocalOntology1() {
		if (btnChooseLocalOntology1 == null) {
			btnChooseLocalOntology1 = new JButton();
			btnChooseLocalOntology1.setText("Browse...");
			btnChooseLocalOntology1.setBounds(new Rectangle(213, 51, 88, 27));
			btnChooseLocalOntology1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					JFileChooser fileChooser = new JFileChooser();					
					fileChooser.setFileFilter(new OwlFilter());
					int returnValue = fileChooser.showOpenDialog(null);
					
					if(returnValue == JFileChooser.APPROVE_OPTION)
					{
						String path = fileChooser.getSelectedFile().getAbsolutePath();
						System.out.println("Caminho do arquivo da ontologia 1 recuperado pela GUI: " + path );
						txtLocalOntology1.setText(path);
					}
				}
			});
		}
		return btnChooseLocalOntology1;
	}

	/**
	 * This method initializes txtLocalOntology1	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtLocalOntology1() {
		if (txtLocalOntology1 == null) {
			txtLocalOntology1 = new JTextField();
			txtLocalOntology1.setBounds(new Rectangle(36, 52, 165, 25));
			txtLocalOntology1.setEditable(false);
		}
		return txtLocalOntology1;
	}

	/**
	 * This method initializes txtLocalOntology2	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtLocalOntology2() {
		if (txtLocalOntology2 == null) {
			txtLocalOntology2 = new JTextField();
			txtLocalOntology2.setBounds(new Rectangle(37, 124, 158, 25));
			txtLocalOntology2.setEditable(false);
		}
		return txtLocalOntology2;
	}

	/**
	 * This method initializes btnChooseLocalOntology2	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtnChooseLocalOntology2() {
		if (btnChooseLocalOntology2 == null) {
			btnChooseLocalOntology2 = new JButton();
			btnChooseLocalOntology2.setText("Browse...");
			btnChooseLocalOntology2.setBounds(new Rectangle(210, 120, 88, 27));
			btnChooseLocalOntology2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileFilter(new OwlFilter());
					int returnValue = fileChooser.showOpenDialog(null);
					
					if(returnValue == JFileChooser.APPROVE_OPTION)
					{
						String path = fileChooser.getSelectedFile().getAbsolutePath();
						System.out.println("Caminho do arquivo da ontologia 2 recuperado pela GUI: " + path );
						txtLocalOntology2.setText(path);
					}
				}
			});
		}
		return btnChooseLocalOntology2;
	}

	/**
	 * This method initializes btnExecuteMatching	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtnExecuteMatching() {
		if (btnExecuteMatching == null) {
			btnExecuteMatching = new JButton();
			btnExecuteMatching.setBounds(new Rectangle(234, 586, 201, 32));
			btnExecuteMatching.setText("Execute semantic matching");
			btnExecuteMatching.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					if(!txtDomainOntology.getText().isEmpty() &&
						!txtLocalOntology1.getText().isEmpty() &&	
						!txtLocalOntology2.getText().isEmpty() )
					{	
						setFacadeWeights();
						//SemanticMatcher semanticMather = new SemanticMatcher();
						matchingFinal = facade.executeSemanticMatchingNormalized(txtLocalOntology1.getText(), txtLocalOntology2.getText(), txtDomainOntology.getText());
						
						fillTable();
						
						JOptionPane.showMessageDialog(null, "Matching Executed!", "SUCCESS" , JOptionPane.INFORMATION_MESSAGE);
						
					}else{
						JOptionPane.showMessageDialog(null, "Choose the 3 ontologies.", "ERROR" , JOptionPane.ERROR_MESSAGE); 
					}
				}
			});
		}
		return btnExecuteMatching;
	}
	
	private void fillTable()
	{
		DefaultTableModel modelo = (DefaultTableModel)tblMatchings.getModel();
        
        
        for (int i = 0; i < tblMatchings.getRowCount(); i++){
            modelo.removeRow(0);            
        }
		
        int cont = 0;
		for(Alignment align : this.matchingFinal)
		{
			modelo.addRow( new String [] {"", ""} );
			
			String subject = align.getSubject().getDescription();
			String predicate = align.getPredicate().split("#")[1];
			String object = align.getObject().getDescription();
			
			tblMatchings.setValueAt(subject, cont, 0);
			tblMatchings.setValueAt(predicate, cont, 1);
			tblMatchings.setValueAt(object, cont, 2);
			tblMatchings.setValueAt(align.getWeight(), cont, 3);
            cont++;
		}
	}

	/**
	 * This method initializes txtDomainOntology	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtDomainOntology() {
		if (txtDomainOntology == null) {
			txtDomainOntology = new JTextField();
			txtDomainOntology.setBounds(new Rectangle(42, 193, 157, 24));
			txtDomainOntology.setEditable(false);
		}
		return txtDomainOntology;
	}

	/**
	 * This method initializes btnChooseDomainOntology	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtnChooseDomainOntology() {
		if (btnChooseDomainOntology == null) {
			btnChooseDomainOntology = new JButton();
			btnChooseDomainOntology.setText("Browse...");
			btnChooseDomainOntology.setSize(new Dimension(88, 27));
			btnChooseDomainOntology.setLocation(new Point(215, 190));
			btnChooseDomainOntology.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileFilter(new OwlFilter());
					int returnValue = fileChooser.showOpenDialog(null);
					
					if(returnValue == JFileChooser.APPROVE_OPTION)
					{
						String path = fileChooser.getSelectedFile().getAbsolutePath();
						System.out.println("Caminho do arquivo da ontologia de dominio recuperado pela GUI: " + path );
						txtDomainOntology.setText(path);
					}
				}
			});
		}
		return btnChooseDomainOntology;
	}

	/**
	 * This method initializes btnSave	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtnSave() {
		if (btnSave == null) {
			btnSave = new JButton();
			btnSave.setBounds(new Rectangle(657, 517, 128, 36));
			btnSave.setText("Save in OWL");
			btnSave.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					if(matchingFinal != null)
					{
						JFileChooser fileChooser = new JFileChooser();
						fileChooser.setFileFilter(new OwlFilter());
						int returnValue = fileChooser.showSaveDialog(null);
						
						if(returnValue == JFileChooser.APPROVE_OPTION)
						{
							String path = fileChooser.getSelectedFile().getAbsolutePath();
							try {
								
								StorageManager.saveSemanticAlignmentsToRDF(matchingFinal, path);
								
							} catch (Exception e1) {
								JOptionPane.showMessageDialog(null, "Problem saving file.", "ERROR" , JOptionPane.ERROR_MESSAGE);
							}
							
							JOptionPane.showMessageDialog(null, "File saved.", "SUCCESS" , JOptionPane.INFORMATION_MESSAGE);							
						}
					}
					else
					{
						JOptionPane.showMessageDialog(null, "Make a matching to save the alignments.", "ERROR" , JOptionPane.ERROR_MESSAGE);
					}
					
				}
			});
		}
		return btnSave;
	}

	/**
	 * This method initializes btnSaveToDB	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtnSaveToDB() {
		if (btnSaveToDB == null) {
			btnSaveToDB = new JButton();
			btnSaveToDB.setBounds(new Rectangle(657, 572, 129, 36));
			btnSaveToDB.setText("Save in DB");
			btnSaveToDB.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					if(matchingFinal != null)
					{
						StorageManager.saveToDB(matchingFinal, "modeloTeste");
						JOptionPane.showMessageDialog(null, "Alignments saved in DB.", "SUCCESS" , JOptionPane.INFORMATION_MESSAGE);
					}else{
						JOptionPane.showMessageDialog(null, "You need to run a matching to save the alignments.", "ERROR" , JOptionPane.ERROR_MESSAGE);
					}
					
				}
			});
		}
		return btnSaveToDB;
	}

	/**
	 * This method initializes pnlScrollableResultMatch	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getPnlScrollableResultMatch() {
		if (pnlScrollableResultMatch == null) {
			pnlScrollableResultMatch = new JScrollPane();
			pnlScrollableResultMatch.setBounds(new Rectangle(327, 22, 459, 483));
			pnlScrollableResultMatch.setViewportView(getTblMatchings());
		}
		return pnlScrollableResultMatch;
	}

	/**
	 * This method initializes btnCalculateSSM	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtnCalculateSSM() {
		if (btnCalculateSSM == null) {
			btnCalculateSSM = new JButton();
			btnCalculateSSM.setBounds(new Rectangle(444, 517, 201, 32));
			btnCalculateSSM.setText("Calculate SSM");
			btnCalculateSSM.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					if(!txtDomainOntology.getText().isEmpty() &&
							!txtLocalOntology1.getText().isEmpty() &&	
							!txtLocalOntology2.getText().isEmpty() )
						{	
							//SemanticMatcher semanticMather = new SemanticMatcher(txtLocalOntology1.getText(), txtLocalOntology2.getText(), txtDomainOntology.getText());
							
							ESSMFunction function = null;
							
							if(rdnAverage.isSelected())
							{
								function = ESSMFunction.Average;								
							}
							else if(rdnDICE.isSelected())
							{
								function = ESSMFunction.DICE;								
							}
							
							if(function != null)
							{
								setFacadeWeights();
								
								String outputPath = txtDomainOntology.getText();
								outputPath = outputPath.substring(0, outputPath.lastIndexOf(File.separator)) + File.separator + "FinalAlignmentsFile.rdf";
								//finalAlignmentsFile = outputPath;
								double result = facade.calculateSSM(txtLocalOntology1.getText(), txtLocalOntology2.getText(), txtDomainOntology.getText(), function, outputPath);//semanticMather.calculateSSM(function, outputPath);
							
								//fillTableWithFinalAlignments();
								
								JOptionPane.showMessageDialog(null, "Similarity measure: " + result, "SUCCESS" , JOptionPane.INFORMATION_MESSAGE);
							}
							else
							{
								JOptionPane.showMessageDialog(null, "Select the type of function to use.", "ERROR" , JOptionPane.ERROR_MESSAGE);								
							}
							
							
						}else{
							JOptionPane.showMessageDialog(null, "Choose the 3 ontologies.", "ERROR" , JOptionPane.ERROR_MESSAGE); 
						}
				}
			});
		}
		return btnCalculateSSM;
	}
	/*
	private void fillTableWithFinalAlignments()
	{
		DefaultTableModel modelo = (DefaultTableModel)tblMatchings.getModel();
        
		ArrayList<Alignment> finalAlignments;
		try {
			finalAlignments = XMLParser.getFileAlignments(this.finalAlignmentsFile);
			for (int i = 0; i < tblMatchings.getRowCount(); i++){
	            modelo.removeRow(0);
	        }
			
	        int cont = 0;
			for(Alignment align : finalAlignments)
			{
				modelo.addRow( new String [] {"", ""} );
				
				String subject = align.getSubject().split("#")[1];
				String predicate = align.getPredicate().split("#")[1];
				String object = align.getObject().split("#")[1];
				
				tblMatchings.setValueAt(subject, cont, 0);
				tblMatchings.setValueAt(predicate, cont, 1);
				tblMatchings.setValueAt(object, cont, 2);
				tblMatchings.setValueAt(align.getWeight(), cont, 3);
	            cont++;
			}
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Problem filling table.", "ERROR" , JOptionPane.ERROR_MESSAGE);
		}
	}*/
	
	/**
	 * This method initializes tblMatchings	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getTblMatchings() {
		if (tblMatchings == null) {
			tblMatchings = new JTable();
		}
		return tblMatchings;
	}

	/**
	 * This method initializes rdnDICE	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRdnDICE() {
		if (rdnDICE == null) {
			rdnDICE = new JRadioButton();
			rdnDICE.setText("DICE");
			rdnDICE.setLocation(new Point(443, 594));
			rdnDICE.setSize(new Dimension(171, 24));
		}
		return rdnDICE;
	}

	/**
	 * This method initializes rdnAverage	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRdnAverage() {
		if (rdnAverage == null) {
			rdnAverage = new JRadioButton();
			rdnAverage.setBounds(new Rectangle(443, 575, 174, 24));
			rdnAverage.setText("Average");
			rdnAverage.setSelected(true);
		}
		return rdnAverage;
	}

	/**
	 * This method initializes btnGenerateAco	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtnGenerateAco() {
		if (btnGenerateAco == null) {
			btnGenerateAco = new JButton();
			btnGenerateAco.setBounds(new Rectangle(26, 587, 202, 31));
			btnGenerateAco.setText("Generate Aco");
			btnGenerateAco.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					
					if(!txtDomainOntology.getText().isEmpty() &&
							!txtLocalOntology1.getText().isEmpty() &&	
							!txtLocalOntology2.getText().isEmpty() )
						{
						
							setFacadeWeights();
							
							facade.generateAcoAlignments(txtLocalOntology1.getText(), txtLocalOntology2.getText(), txtDomainOntology.getText());
							
							JOptionPane.showMessageDialog(null, "Aco alignments generated!", "SUCCESS" , JOptionPane.INFORMATION_MESSAGE);
						}
				}
			});
		}
		return btnGenerateAco;
	}
	private JLabel getLblIs() {
		if (lblIs == null) {
			lblIs = new JLabel("isSubConceptOf:");
			lblIs.setBounds(31, 174, 128, 16);
			lblIs.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
		}
		return lblIs;
	}
	private JLabel getLblIssuperconceptof() {
		if (lblIssuperconceptof == null) {
			lblIssuperconceptof = new JLabel("isSuperConceptOf:");
			lblIssuperconceptof.setBounds(31, 202, 128, 16);
			lblIssuperconceptof.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
		}
		return lblIssuperconceptof;
	}
	private JLabel getLblIs_1() {
		if (lblIs_1 == null) {
			lblIs_1 = new JLabel("isPartOf:");
			lblIs_1.setBounds(31, 230, 128, 16);
			lblIs_1.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
		}
		return lblIs_1;
	}
	private JLabel getLblIswholeof() {
		if (lblIswholeof == null) {
			lblIswholeof = new JLabel("isWholeOf:");
			lblIswholeof.setBounds(31, 258, 128, 16);
			lblIswholeof.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
		}
		return lblIswholeof;
	}
	private JLabel getLblIsdisjointwith() {
		if (lblIsdisjointwith == null) {
			lblIsdisjointwith = new JLabel("isDisjointWith:");
			lblIsdisjointwith.setBounds(31, 286, 128, 16);
			lblIsdisjointwith.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
		}
		return lblIsdisjointwith;
	}
	private JLabel getLblIscloseto() {
		if (lblIscloseto == null) {
			lblIscloseto = new JLabel("isCloseTo:");
			lblIscloseto.setBounds(31, 316, 128, 16);
			lblIscloseto.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
		}
		return lblIscloseto;
	}
	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setBorder(new TitledBorder(null, "Weight Properties", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panel.setBounds(26, 227, 281, 350);
			panel.setLayout(null);
			
			JLabel lblDefine = new JLabel("Define Semantic Relations Weight:");
			lblDefine.setBounds(17, 109, 222, 16);
			panel.add(lblDefine);
			
			JLabel lblNewLabel = new JLabel("isEquivalentTo:");
			lblNewLabel.setBounds(31, 143, 128, 16);
			panel.add(lblNewLabel);
			lblNewLabel.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
			panel.add(getLblIs());
			panel.add(getLblIssuperconceptof());
			panel.add(getLblIs_1());
			panel.add(getLblIswholeof());
			panel.add(getLblIsdisjointwith());
			panel.add(getLblIscloseto());
			
			inputEquivalent = new JTextField();
			inputEquivalent.setBounds(201, 137, 63, 28);
			inputEquivalent.setText(facade.getDefaultWeight().get(WeightParam.EQUIVALENT) + "");
			panel.add(inputEquivalent);
			inputEquivalent.setColumns(5);
			
			inputSubConcept = new JTextField();
			inputSubConcept.setBounds(201, 168, 63, 28);
			inputSubConcept.setText(facade.getDefaultWeight().get(WeightParam.SUB_CONCEPT) + "");
			panel.add(inputSubConcept);
			inputSubConcept.setColumns(5);
			
			inputSuperConcept = new JTextField();
			inputSuperConcept.setBounds(201, 196, 63, 28);
			inputSuperConcept.setText(facade.getDefaultWeight().get(WeightParam.SUPER_CONCEPT) + "");
			panel.add(inputSuperConcept);
			inputSuperConcept.setColumns(5);
			
			inputPart = new JTextField();
			inputPart.setBounds(201, 224, 63, 28);
			inputPart.setText(facade.getDefaultWeight().get(WeightParam.PART_OF) + "");
			panel.add(inputPart);
			inputPart.setColumns(5);
			
			inputWhole = new JTextField();
			inputWhole.setBounds(201, 252, 63, 28);
			inputWhole.setText(facade.getDefaultWeight().get(WeightParam.WHOLE_OF) + "");
			panel.add(inputWhole);
			inputWhole.setColumns(5);
			
			inputDisjoint = new JTextField();
			inputDisjoint.setBounds(201, 280, 63, 28);
			inputDisjoint.setText(facade.getDefaultWeight().get(WeightParam.DISJOINT) + "");
			panel.add(inputDisjoint);
			inputDisjoint.setColumns(5);
			
			inputClose = new JTextField();
			inputClose.setBounds(201, 310, 63, 28);
			inputClose.setText(facade.getDefaultWeight().get(WeightParam.CLOSE_TO) + "");
			panel.add(inputClose);
			inputClose.setColumns(5);
			
			panel.add(getLblDefineLinguisticAnd());
			panel.add(getInputLE());
			panel.add(getLblSemanticMatchingWeight());
			panel.add(getInputSemantic());
		}
		return panel;
	}
	private JLabel getLblDefineLinguisticAnd() {
		if (lblLEMatchingWeight == null) {
			lblLEMatchingWeight = new JLabel("<html>Linguistic-Structural<br>Matching Weight:</html>");
			lblLEMatchingWeight.setHorizontalAlignment(SwingConstants.LEFT);
			lblLEMatchingWeight.setBounds(17, 24, 177, 38);
		}
		return lblLEMatchingWeight;
	}
	private JTextField getInputLE() {
		if (inputLE == null) {
			inputLE = new JTextField();
			inputLE.setBounds(201, 29, 63, 28);
			inputLE.setColumns(10);
			inputLE.setText(facade.getDefaultWeight().get(WeightParam.LE_MATCH) + "");
		}
		return inputLE;
	}
	private JLabel getLblSemanticMatchingWeight() {
		if (lblSemanticMatchingWeight == null) {
			lblSemanticMatchingWeight = new JLabel("Semantic Matching Weight:");
			lblSemanticMatchingWeight.setBounds(17, 75, 177, 16);
		}
		return lblSemanticMatchingWeight;
	}
	private JTextField getInputSemantic() {
		if (inputSemantic == null) {
			inputSemantic = new JTextField();
			inputSemantic.setBounds(201, 69, 63, 28);
			inputSemantic.setColumns(10);
			inputSemantic.setText(facade.getDefaultWeight().get(WeightParam.SEMANTIC_MATCH) + "");
		}
		return inputSemantic;
	}
	
	private void setFacadeWeights(){
		
		double wLE = Double.valueOf(inputLE.getText());
		double wSemantic = Double.valueOf(inputSemantic.getText());
		double wEquivalent = Double.valueOf(inputEquivalent.getText());
		double wSubConcept = Double.valueOf(inputSubConcept.getText());
		double wSuperConcept = Double.valueOf(inputSuperConcept.getText());
		double wPart = Double.valueOf(inputPart.getText());
		double wWhole = Double.valueOf(inputWhole.getText());
		double wDisjoint = Double.valueOf(inputDisjoint.getText());
		double wClose = Double.valueOf(inputClose.getText());
		
		Map<WeightParam, Double> weights = new HashMap<WeightParam, Double>();
		weights.put(WeightParam.LE_MATCH, wLE);
		weights.put(WeightParam.SEMANTIC_MATCH, wSemantic);
		weights.put(WeightParam.EQUIVALENT, wEquivalent);
		weights.put(WeightParam.SUB_CONCEPT, wSubConcept);
		weights.put(WeightParam.SUPER_CONCEPT, wSuperConcept);
		weights.put(WeightParam.PART_OF, wPart);
		weights.put(WeightParam.WHOLE_OF, wWhole);
		weights.put(WeightParam.CLOSE_TO, wClose);
		weights.put(WeightParam.DISJOINT, wDisjoint);

		facade.setWeights(weights);

	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
