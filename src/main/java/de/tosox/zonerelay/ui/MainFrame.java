package de.tosox.zonerelay.ui;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.tosox.zonerelay.BuildInfo;
import de.tosox.zonerelay.shared.i18n.Localizer;
import de.tosox.zonerelay.ui.util.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The {@code MainFrame} class represents the main application window.
 */
@Singleton
public class MainFrame extends JFrame {
	private final Localizer localizer;
	private final ImageLoader imageLoader;
	private final JTextPane txpOutput;

	private JCheckBox cbxFullInstall = new JCheckBox();
	private JProgressBar pgbCurrent = new JProgressBar();
	private JProgressBar pgbTotal = new JProgressBar();

	private MainFrameController controller;

	@Inject
	public MainFrame(Localizer localizer, ImageLoader imageLoader, JTextPane txpOutput) {
		this.localizer = localizer;
		this.imageLoader = imageLoader;
		this.txpOutput = txpOutput;

		BufferedImage icon = imageLoader.load(getClass().getResource("icon.png"));

		initializeComponents();

		this.setTitle(BuildInfo.APP_NAME);
		this.setSize(800, 600);
		this.setOpacity(0.9f);
		this.setResizable(false);
		this.setIconImage(icon);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Inject
	public void setController(MainFrameController controller) {
		this.controller = controller;
	}

	/**
	 * Makes the main window visible.
	 */
	public void showWindow() {
		this.setVisible(true);
	}

	public boolean isFullInstallSelected() {
		return cbxFullInstall.isSelected();
	}

	public void setCurrentProgress(int value) {
		SwingUtilities.invokeLater(() -> {
			if (pgbCurrent.isIndeterminate()) {
				pgbCurrent.setIndeterminate(false);
				pgbCurrent.setString(null);
			}
			pgbCurrent.setValue(value);
		});
	}

	public void setCurrentProgressIndeterminate() {
		SwingUtilities.invokeLater(() -> {
			if (!pgbCurrent.isIndeterminate()) {
				pgbCurrent.setIndeterminate(true);
				pgbCurrent.setString(localizer.translate("GUI_DOWNLOADING"));
			}
		});
	}

	public void setTotalProgress(int value) {
		SwingUtilities.invokeLater(() -> pgbTotal.setValue(value));
	}

	/**
	 * This overrides the {@link java.awt.Frame#isUndecorated()} method
	 * so the opacity can be set without undecorating the window.
	 *
	 * @return true
	 */
	@Override
	public boolean isUndecorated() {
		return true;
	}

	/**
	 * Initializes the main components of the main frame.
	 */
	private void initializeComponents() {
		JPanel pnlMain = new JPanel(null);
		{
			JPanel pnlOptions = new JPanel(null);
			{
				BufferedImage logo = imageLoader.load(getClass().getResource("logo.png"));
				JImagePanel imgLogo = new JImagePanel(logo);
				imgLogo.setBackground(new Color(0x03, 0x03, 0x03));
				imgLogo.setBounds(10, 10, 160, 80);
				pnlOptions.add(imgLogo);

				JLabel lblVersion = new JLabel(localizer.translate("GUI_INSTALLER_VERSION", BuildInfo.VERSION), SwingConstants.CENTER);
				lblVersion.setForeground(new Color(0xC2, 0xC2, 0xC2));
				lblVersion.setBounds(10, 90, 160, 30);
				pnlOptions.add(lblVersion);

				JSeparator sepLogo = new JSeparator();
				sepLogo.setBounds(20, 115, 140, 2);
				sepLogo.setForeground(Color.WHITE);
				pnlOptions.add(sepLogo);

				JButton btnInstall = new JButton(localizer.translate("GUI_INSTALL"));
				btnInstall.setBounds(10, 135, 160, 80);
				btnInstall.setBackground(new Color(0x03, 0x03, 0x03));
				btnInstall.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, false));
				btnInstall.addActionListener(a -> controller.onInstallClick());
				pnlOptions.add(btnInstall);

				cbxFullInstall = new JCheckBox(localizer.translate("GUI_FULL_INSTALLATION"));
				cbxFullInstall.setBounds(20, 215, 160, 30);
				pnlOptions.add(cbxFullInstall);

				JSeparator sepInstall = new JSeparator();
				sepInstall.setBounds(20, 260, 140, 2);
				sepInstall.setForeground(Color.WHITE);
				pnlOptions.add(sepInstall);

				JButton btnLaunch = new JButton(localizer.translate("GUI_LAUNCH_MO2"));
				btnLaunch.setBounds(10, 280, 160, 80);
				btnLaunch.setBackground(new Color(0x03, 0x03, 0x03));
				btnLaunch.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, false));
				btnLaunch.addActionListener(a -> controller.onLaunchClick());
				pnlOptions.add(btnLaunch);

				pnlOptions.setBackground(new Color(0x03, 0x03, 0x03));
				pnlOptions.setBounds(0, 0, 180, 600);
				pnlMain.add(pnlOptions);
			}

			JPanel pnlOutput = new JPanel(null);
			{
				txpOutput.setBackground(new Color(0x06, 0x06, 0x06));
				txpOutput.setEditable(false);

				JScrollPane scpOutput = new JScrollPane(txpOutput);
				scpOutput.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, false));
				scpOutput.setBounds(10, 10, 580, 480);
				pnlOutput.add(scpOutput);

				JLabel lblCurrent = new JLabel(localizer.translate("GUI_CURRENT_PROGRESS"));
				lblCurrent.setBounds(10, 495, 90, 30);
				pnlOutput.add(lblCurrent);

				pgbCurrent = new JProgressBar();
				pgbCurrent.setStringPainted(true);
				pgbCurrent.setBounds(110, 500, 480, 20);
				pnlOutput.add(pgbCurrent);

				JLabel lblTotal = new JLabel(localizer.translate("GUI_TOTAL_PROGRESS"));
				lblTotal.setBounds(10, 525, 90, 30);
				pnlOutput.add(lblTotal);

				pgbTotal = new JProgressBar();
				pgbTotal.setStringPainted(true);
				pgbTotal.setBounds(110, 530, 480, 20);
				pnlOutput.add(pgbTotal);

				pnlOutput.setBackground(new Color(0x03, 0x03, 0x03));
				pnlOutput.setBounds(180, 0, 640, 600);
				pnlMain.add(pnlOutput);
			}

			this.add(pnlMain);
		}
	}
}
