import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import gamepatcher.Downloader;

public class Main {
	private Downloader downloader;
	private JFrame frame;
	public static void main(String[] args){
        final Main main = new Main();
        main.scanAndDeleteOldFiles("changelog", ".txt");
		main.downloader = new Downloader("PixelZombies.zip", "version.txt", "http://hardlocked-games.zz.mu/download/games/dug-out/spec_ops/Dug_Out-Spec_Ops", "http://hardlocked-games.zz.mu/download/games/versions/dug-out/spec_ops/version.txt", 1000);
		if(main.downloader.isUpdateNecessary()){
			main.frame = new JFrame("HardLocked Games - Game Launcherr");
			main.frame.setContentPane(new JPanel(new BorderLayout()));
	        main.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        main.frame.setResizable(false);
	        main.frame.setLocationRelativeTo(null);
	        System.out.println("Downloading");
	        main.addComponents();
		}
		else {
			if(main.downloader.getDownloaded()){
				try {
					extractFolder(System.getProperty("user.dir") + File.separatorChar + "Dug_out-Spec_Ops.zip");
					System.out.println("Game Extracted");
				} catch (ZipException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(new File(System.getProperty("user.dir") + File.separatorChar + "dug-out_spec-ops.jar").exists()){
				System.out.println("Launching game...");
				ProcessBuilder pb = new ProcessBuilder("java", "-jar", System.getProperty("user.dir") + File.separatorChar + "Dug_Out-Spec_Ops.jar");
				pb.directory(new File(System.getProperty("user.dir")));
				try {
					pb.inheritIO();
					pb.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else
				System.out.println("Couldn't find required game files... Please reload the launcher");
		}
	}
	private void addComponents(){
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        JTextField text = new JTextField("Would you like to download the latest update?");
        text.setHorizontalAlignment(JTextField.CENTER);
        text.setEditable(false);
        text.setHighlighter(null);
        mainPanel.add(text);
        int width = text.getFontMetrics(text.getFont()).stringWidth(text.getText()) + 6;
        JPanel yesno = new JPanel();
        yesno.setLayout(new BoxLayout(yesno, BoxLayout.X_AXIS));
        JButton yes = new JButton("Yes");
        yes.setPreferredSize(new Dimension(width / 2, 20));
        yes.setMaximumSize(new Dimension(width / 2, 20));
        yes.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		downloader.downloadFiles();
        		buttonClicked();
        	}
        });
        yesno.add(yes, BorderLayout.LINE_START);
        JButton no = new JButton("No");
        no.setPreferredSize(new Dimension(width / 2, 20));
        no.setMaximumSize(new Dimension(width / 2, 20));
        no.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		buttonClicked();
        	}
        });
        yesno.add(no, BorderLayout.LINE_END);
        mainPanel.add(yesno);
        JTextArea changeLog = new JTextArea();
        changeLog.setEditable(false);
        changeLog.setBackground(text.getBackground());
        changeLog.setLineWrap(true);
        changeLog.setWrapStyleWord(true);
        changeLog.setHighlighter(null);
        File changelog = getChangelog();
        BufferedReader br = null;
        FileReader reader = null;
        try {
			reader = new FileReader(changelog);
			br = new BufferedReader(reader);
			String line;
			while((line = br.readLine()) != null){
				if(line.startsWith("\t"))
					line = "   " + line.substring(1);
				changeLog.append(line + '\n');
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if(br != null)
				try {
					br.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			if(reader != null)
				try {
					reader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
        JScrollPane scroll = new JScrollPane(changeLog);
        scroll.setPreferredSize(new Dimension(220, 100));
        mainPanel.add(scroll);
        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);
	}
	private static void extractFolder(String zipFile) throws ZipException, IOException {
	    System.out.println("Extracting game files...");
	    int BUFFER = 2048;
	    File file = new File(zipFile);
	    @SuppressWarnings("resource")
		ZipFile zip = new ZipFile(file);
	    String newPath = zipFile.substring(0, zipFile.lastIndexOf(File.separatorChar));
	    new File(newPath).mkdir();
	    Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
	    while(zipFileEntries.hasMoreElements()){
	        ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
	        String currentEntry = entry.getName();
	        File destFile = new File(newPath, currentEntry);
	        File destinationParent = destFile.getParentFile();
	        destinationParent.mkdirs();
	        if(!entry.isDirectory()){
	            BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
	            int currentByte;
	            byte data[] = new byte[BUFFER];
	            FileOutputStream fos = new FileOutputStream(destFile);
	            BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
	            while((currentByte = is.read(data, 0, BUFFER)) != -1){
	                dest.write(data, 0, currentByte);
	            }
	            dest.flush();
	            dest.close();
	            is.close();
	        }
	        if(currentEntry.endsWith(".zip")){
	            extractFolder(destFile.getAbsolutePath());
	        }
	    }
	}
	private void buttonClicked(){
		frame.dispose();
		if(downloader.getDownloaded()){
			try {
				extractFolder(System.getProperty("user.dir") + File.separatorChar + "Dug_Out-Spec_Ops.zip");
				System.out.println("Game Extracted");
			} catch (ZipException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(new File(System.getProperty("user.dir") + File.separatorChar + "Dug_Out-Spec_Ops.jar").exists()){
			System.out.println("Launching game...");
			ProcessBuilder pb = new ProcessBuilder("java", "-jar", System.getProperty("user.dir") + File.separatorChar + "Dug_Out-Spec_Ops.jar");
			pb.directory(new File(System.getProperty("user.dir")));
			try {
				pb.inheritIO();
				pb.start();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else
			System.out.println("Could not find jar file, will not launch");
	}
	private File getChangelog(){
		File changelog = null;
		FileOutputStream fos = null;
		ReadableByteChannel rbc = null;
		try {
			URL site = new URL("http://hardlocked-games.zz.mu/dug-out/spec-ops/update.php");
			URLConnection siteConnection = site.openConnection();
			long size = siteConnection.getContentLength();
			rbc = Channels.newChannel(site.openStream());
			changelog = File.createTempFile("update", ".php");
			changelog.deleteOnExit();
			fos = new FileOutputStream(changelog);
			fos.getChannel().transferFrom(rbc, 0, size);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(rbc != null)
				try {
					rbc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if(fos != null)
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return changelog;
	}
	private void scanAndDeleteOldFiles(String name, String suffix){
		DirectoryStream<Path> ds = null;
		try {
			ds = Files.newDirectoryStream(Paths.get(System.getProperty("java.io.tmpdir")), name + '*' + suffix);
			for(Path file : ds){
				if(file.toFile().delete())
					System.out.println("Old file " + file.toFile().getAbsolutePath() + " Old Files Deleted");
				else
					System.out.println("Old file " + file.toFile().getAbsolutePath() + "Failed To Delete Old Game Files");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ds.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
