package GUI;

//��Ϣ

@SuppressWarnings("serial")
public class Message extends javax.swing.JDialog {

	/** Creates new form Message */
	String m;

	public Message(String t, String s) {
		super(new javax.swing.JFrame(), true);
		m = s;
		initComponents();
		setTitle(t);
		setLocation(400, 300);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	//GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		jButton1 = new javax.swing.JButton();
		jLabel1 = new javax.swing.JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		jButton1.setText("ȷ��");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});

		jLabel1.setFont(new java.awt.Font("΢���ź�", 1, 12));
		//jLabel1.setForeground(new java.awt.Color(255, 0, 0));
		jLabel1.setText(m);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				javax.swing.GroupLayout.Alignment.TRAILING,
				layout.createSequentialGroup().addContainerGap(130,
						Short.MAX_VALUE).addComponent(jButton1)
						.addContainerGap()).addGroup(
				layout.createSequentialGroup().addGap(32, 32, 32).addComponent(
						jLabel1).addContainerGap(147, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				javax.swing.GroupLayout.Alignment.TRAILING,
				layout.createSequentialGroup().addGap(35, 35, 35).addComponent(
						jLabel1).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED, 55,
						Short.MAX_VALUE).addComponent(jButton1)
						.addContainerGap()));

		pack();
	}// </editor-fold>
	//GEN-END:initComponents

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
		setVisible(false);
		dispose();
	}

	//GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.JButton jButton1;
	private javax.swing.JLabel jLabel1;
	// End of variables declaration//GEN-END:variables

}