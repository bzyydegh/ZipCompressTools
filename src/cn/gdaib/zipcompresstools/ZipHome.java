package cn.gdaib.zipcompresstools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * 主界面
 * @Author:zhuyy
 * 创建于2016-02-26
 */
@SuppressWarnings("serial")
public class ZipHome extends JFrame{
	private JTextField path1, path2;
	private JTextArea descArea;
	private JButton selectDirect1, selectDirect2;
	private JFileChooser selectDialog = new JFileChooser();
	private File file;
	private JScrollPane scrollPane;
	private JProgressBar jProgressBar = new JProgressBar(0, 100);
	private Color bgBlue;
	private long totalByte;
	private int currentValue = 0;
	private int total = 0;
	private final String startDescription = "************欢迎使用压缩小工具************\n\n说明:\n****1.这是一个解压缩程序，无需安装，双击或鼠标右键选择打开即可运行\n****2.您可以使用它来进行压缩文件或解压文件包\n****3.此版本目前只支持.zip格式,请不要选择其他格式的压缩包来进行解压，否则解压失败！！\n****4.请先在《《原始文件路径》》中选择需要压缩的文件夹及文件或者需要解压的文件包\n****5.在《《输出文件路径》》中可以更改默认的压缩或解压路径\n\n*****当前版本:V2.1.0\n*****作者:zhuyy";
	private String fileAddressNotFormat;//文件地址，不带格式
	private String fileNameNotFormat;//文件名，不带格式
	private boolean isFile;//是文件还是目录
	private boolean isCompressFile;//是否是压缩包

	// 初始化界面
	public ZipHome(String title) throws HeadlessException {
		super(title);
		// -------------------------顶部面板
		JPanel jp_north = new JPanel(new GridLayout(2, 1));

		JPanel jp_north_1 = new JPanel();
		jp_north_1.add(new JLabel("原始文件路径："));
		path1 = new JTextField(38);
		jp_north_1.add(path1);
		selectDirect1 = new JButton("选择文件");
		selectDirect1.setBackground(Color.yellow);
		selectDirect1.addActionListener(new SelectDirect());
		jp_north_1.add(selectDirect1);

		JPanel jp_north_2 = new JPanel();
		jp_north_2.add(new JLabel("输出文件路径："));
		path2 = new JTextField(38);
		jp_north_2.add(path2);
		selectDirect2 = new JButton("更改路径");
		selectDirect2.setBackground(Color.yellow);
		selectDirect2.addActionListener(new SelectDirect());
		jp_north_2.add(selectDirect2);

		jp_north.add(jp_north_1);
		jp_north.add(jp_north_2);

		// --------------------------中部面板
		JPanel jp_center = new JPanel();
		descArea = new JTextArea(10, 53);
		StringBuffer sb = new StringBuffer(startDescription);
		descArea.append(sb.toString());
		scrollPane = new JScrollPane(descArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jp_center.add(scrollPane);

		// --------------------------底部面板
		JPanel jp_south = new JPanel(new GridLayout(2, 1));

		JPanel jp_south_1 = new JPanel();
		JButton btn_zip = new JButton("压缩");
		btn_zip.setBackground(Color.orange);
		btn_zip.addActionListener(new ZipAction());

		JButton btn_unzip = new JButton("解压");
		btn_unzip.setBackground(Color.orange);
		btn_unzip.addActionListener(new UnzipAction());
		jp_south_1.add(btn_zip);
		jp_south_1.add(btn_unzip);

		JPanel jp_south_2 = new JPanel();
		// jProgressBar.setValue(50);
		jp_south_2.setLayout(null);
		JLabel jpb_label = new JLabel("完成进度:");
		jpb_label.setBounds(20, 0, 80, 20);
		jProgressBar.setBounds(82, 0, 520, 20);
		bgBlue = new Color(65, 105, 225);
		jProgressBar.setForeground(bgBlue);

		jp_south_2.add(jpb_label);
		jp_south_2.add(jProgressBar);
		jp_south.add(jp_south_2);
		jp_south.add(jp_south_1);

		// 整个小工具界面Frame由上中下三个面板组成
		this.add(jp_north, BorderLayout.NORTH);
		this.add(jp_center, BorderLayout.CENTER);
		this.add(jp_south, BorderLayout.SOUTH);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// 设置窗口的小图标
		this.setIconImage(
				Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("images/izip.png")));
		this.pack();
		this.setResizable(false);
		// 获取屏幕宽度、高度
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// 使窗口居于屏幕中间
		int left = (screenSize.width - this.getWidth()) / 2;
		int top = (screenSize.height - this.getHeight()) / 2;
		this.setLocation(left, top);
		// 窗口可见
		this.setVisible(true);
	}

	/**
	 * 文件选择
	 */
	private class SelectDirect implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// 使进度条清零
			jProgressBar.setValue(0);
			
			if(e.getSource() == selectDirect1) {
				// JFileChooser默认的是选择文件，
				// 选目录要将DIRECTORIES_ONLY装入模型
				// 若选择文件，则无需此句
				selectDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int intRetVal = selectDialog.showOpenDialog(getParent());
				file = selectDialog.getSelectedFile();
				if(intRetVal == JFileChooser.APPROVE_OPTION) {
					if(file.isDirectory()) {// 选择的是目录，压缩
						// System.out.println("文件名:"+MODE);
						isFile = false;
						isCompressFile = false;
						path1.setText(selectDialog.getSelectedFile().getPath());
						fileAddressNotFormat = getFileAddressNoneDot(path1.getText() + ".JPEG");
						fileNameNotFormat = getFileNameNoneDot(path1.getText() + ".JPEG");
						path2.setText(fileAddressNotFormat + ".zip");
					}else if(file.isFile()) {
						// System.out.println("文件:"+MODE);
						isFile = true;
						path1.setText(selectDialog.getSelectedFile().getPath());
						//获取到的文件格式
						String format=getFileFormat(path1.getText());
						fileNameNotFormat=getFileNameNoneDot(path1.getText());
						fileAddressNotFormat = getFileAddressNoneDot(path1.getText());
						// System.out.println(fileAddressNotFormat);
						if(".zip".equals(format)||".rar".equals(format)){
							path2.setText(fileAddressNotFormat);
							isCompressFile=true;
							//System.out.println(fileAddressNotFormat);
						}else{
							path2.setText(fileAddressNotFormat + ".zip");
							isCompressFile=false;
							//System.out.println(fileAddressNotFormat);
						}
					}
				}
			}else if(e.getSource() == selectDirect2) {
				if ("".equals(path1.getText())) {
					JOptionPane.showMessageDialog(getParent(), "请先选择原始文件路径");// 弹出提示信息
				} else {
					selectDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int intRetVal = selectDialog.showOpenDialog(getParent());
					if (intRetVal == JFileChooser.APPROVE_OPTION) {	
						if(isCompressFile){
							path2.setText(selectDialog.getSelectedFile().getPath() +"\\"+fileNameNotFormat);
						}else{
							path2.setText(selectDialog.getSelectedFile().getPath() +"\\"+fileNameNotFormat+".zip");
						}
						
					}
				}
			}
		}
	}

	// 获取文件名,包括文件格式
	private String getFileName(String fileAddress) {
		String tempFile = fileAddress.trim();
		File file = new File(tempFile);
		String fileName = file.getName();
		// System.out.println("getFileName--"+fileName);
		return fileName;
	}

	// 获取文件名，不包括格式
	private String getFileNameNoneDot(String fileAddress) {
		String fileName = getFileName(fileAddress);
		String tempFile[] = fileName.split("\\.");
		String fileNameNoneDot = tempFile[tempFile.length - 2];
		//System.out.println("getFileNameNoneDot--"+fileNameNoneDot);
		return fileNameNoneDot;
	}

	// 获取文件路径，不包括其中文件格式
	private String getFileAddressNoneDot(String fileAddress) {
		String tempFile[] = fileAddress.split("\\.");
		String fileNameNoneDot = tempFile[tempFile.length - 2];
		// System.out.println("getFileAddressNoneDot--"+fileNameNoneDot);
		return fileNameNoneDot;
	}

	//获取文件格式
	private String getFileFormat(String fileAddress) {
		String fileName = getFileName(fileAddress);
		String tempFile[] = fileName.split("\\.");
		String fileNameNoneDot = tempFile[tempFile.length - 1];
		//System.out.println("getFileNameNoneDot--"+fileNameNoneDot);
		return "."+fileNameNoneDot;
	}
	
	/**
	 * 处理压缩
	 */
	private class ZipAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			//获取到的文件格式
			String format=getFileFormat(path1.getText());
			// 使进度条清零
			jProgressBar.setValue(0);
			// 原文件路径
			String sourcePath = path1.getText();
			// 输出文件路径
			String outPath = path2.getText();
			if("".equals(sourcePath)) {
				JOptionPane.showMessageDialog(getParent(), "请输入原始文件路径！");// 弹出提示信息
			}else if("".equals(outPath)) {
				JOptionPane.showMessageDialog(getParent(), "请输入输出文件路径！");
			}else if(isFile&&".zip".equals(format)||".rar".equals(format)||".jar".equals(format)){
				JOptionPane.showMessageDialog(getParent(), "您选择的文件已经是个压缩包了！");
			}else {
				// String result=new ZipFileMgr().zip(sourcePath, outPath);
				String result = new ZipFileMgrTest().zip(sourcePath, outPath);
				descArea.setText(result);
			}
		}

	}

	/**
	 * 处理解压
	 */
	private class UnzipAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			//获取到的文件格式
			String format=getFileFormat(path1.getText());
			// 使进度条清零
			jProgressBar.setValue(0);
			// 原文件路径
			String sourcePath = path1.getText();
			// 输出文件路径
			String outPath = path2.getText();
			if("".equals(sourcePath)) {
				JOptionPane.showMessageDialog(getParent(), "请输入原始文件路径！");
			}else if("".equals(outPath)) {
				JOptionPane.showMessageDialog(getParent(), "请输入输出文件路径！");
			}else if(!isFile){
				JOptionPane.showMessageDialog(getParent(), "您选择的是文件夹！");
			}else if(isFile&&!".zip".equals(format)){
				JOptionPane.showMessageDialog(getParent(), "您选择的文件不是.zip格式！");
			}else{
				// String result=new UnZipFileMgr().unZip(sourcePath, outPath);
				String result = new UnZipFileMgrTest().unZip(sourcePath, outPath);
				descArea.setText(result);
			}
		}

	}

	/**
	 * 解压部分 创建于2016-02-26
	 */
	private class UnZipFileMgrTest {

		/**
		 * 解压文件或文件夹
		 * @param sourcePath 源文件路径（将要压缩的文件夹）
		 * @param outPath 输出文件的路径
		 */
		public String unZip(String sourcePath, String outPath) {
			jProgressBar.setString("正在解压中:");
			try {
				StringBuffer sb = new StringBuffer("解压文件如下：\n");
				// 文件输入流
				FileInputStream fin = new FileInputStream(sourcePath);
				totalByte = fin.available();
				// 所读取数据校验和的输入流。校验和可用于验证输入数据的完整性
				CheckedInputStream checkedInputStream = new CheckedInputStream(fin, new Adler32());
				// zip格式的输入流
				ZipInputStream zin = new ZipInputStream(fin);
				ZipEntry zipEntry;
				// 初始化变量值
				total = 0;
				currentValue = 0;
				totalByte = zin.available();
				// 遍历压缩文件中的所有压缩条目
				while ((zipEntry = zin.getNextEntry()) != null) {
					sb.append(zipEntry.getName()).append("\n");
					// 将压缩条目输出到输出路径
					File targetFile = new File(outPath + File.separator + zipEntry.getName());
					// 判断目标输出文件的父文件夹是否存在，如果不存在的话则需要创建一个文件夹
					if (!targetFile.getParentFile().exists()) {
						// 不存在父文件夹
						targetFile.getParentFile().mkdirs();
					}
					if (zipEntry.isDirectory()) {
						// 判断当前的压缩条目是否是文件夹，如果是文件夹的话，直接在输出路径创建文件即可
						targetFile.mkdirs();
					} else {
						// 当前的压缩条目是一个文件，需要将输入的压缩文件内容输出到输出路径
						FileOutputStream fout = new FileOutputStream(targetFile);
						byte[] buff = new byte[1024];
						int length;
						while ((length = zin.read(buff)) != -1) {
							fout.write(buff, 0, length);
							total += length;
							currentValue = (int) (total * 100 / totalByte);
							jProgressBar.setValue(currentValue);
						}
						fout.close();
					}
				}
				zin.close();
				System.out.println("unZip:total" + total + "\n");
				// System.out.println("校验和为："+checkedInputStream.getChecksum().getValue());
				sb.append("校验和为：" + checkedInputStream.getChecksum().getValue()).append("\n");
				sb.append("解压结束");
				checkedInputStream.close();
				fin.close();
				jProgressBar.setValue(100);
				return sb.toString();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(getParent(), "很抱歉，解压出错！请检查文件选择是否正确");
				e.printStackTrace();
			}
			return "";
		}
	}

	/**
	 * 压缩部分 创建于2016-02-26
	 */
	private class ZipFileMgrTest {
		StringBuffer sb;

		/**
		 * 压缩文件或文件夹
		 * @param sourcePath  源文件路径（将要压缩的文件）
		 * @param outPath  压缩条目（实际上就是一个文件的相对路径）
		 */
		public String zip(String sourcePath, String outPath) {
			jProgressBar.setString("正在压缩中:");
			try {
				sb = new StringBuffer("压缩文件如下：\n");
				// 文件输出流
				FileOutputStream fout = new FileOutputStream(outPath);
				// 写入数据校验和的输出流。校验和可用于验证输出数据的完整性
				CheckedOutputStream checkedOutputStream = new CheckedOutputStream(fout, new Adler32());
				// zip格式的输出流
				ZipOutputStream zout = new ZipOutputStream(fout);
				// 要将一个源文件写入到一个压缩文件中
				File sourceFile = new File(sourcePath);
				// 压缩条目
				String zipEntryName = sourceFile.getName();

				if (sourceFile.isFile()) {
					// 压缩文件
					zipFile(zout, sourceFile, zipEntryName);
				} else {
					// 压缩文件夹
					zipDirectory(zout, sourceFile, zipEntryName);
				}

				zout.close();
				// System.out.println("校验和为："+checkedOutputStream.getChecksum().getValue());
				sb.append("校验和为：" + checkedOutputStream.getChecksum().getValue()).append("\n");
				sb.append("压缩完成。");
				checkedOutputStream.close();
				fout.close();
				return sb.toString();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(getParent(), "很抱歉，压缩出错！请检查文件选择是否正确");
				e.printStackTrace();
			}
			return "";
		}

		/**
		 * 压缩文件夹
		 * @param zout zip格式的文件输出流
		 * @param sourceFile 源文件路径（将要压缩的文件）
		 * @param zipEntryName 压缩条目（实际上就是一个文件的相对路径）
		 */
		private void zipDirectory(ZipOutputStream zout, File sourceFile, String zipEntryName)
				throws IOException, FileNotFoundException {
			// 压缩目录，遍历目录里的所有文件
			for (File file : sourceFile.listFiles()) {
				if (file.isFile()) {
					// 如果是文件，则直接调用压缩文件的方法进行压缩
					zipFile(zout, file, zipEntryName + "/" + file.getName());
				} else {
					// 说明现在的file是目录，则需要将该目录的所有文件压缩
					if (file.listFiles().length > 0) {
						// 递归调用压缩文件夹的方法
						zipDirectory(zout, file, zipEntryName + "/" + file.getName());// 资料/文档
					} else {
						// 空文件夹
						// 将压缩条目写入到压缩对象中
						zout.putNextEntry(new ZipEntry(zipEntryName + "/" + file.getName() + "/"));
						zout.closeEntry();
						sb.append(zipEntryName + "/" + file.getName() + "/").append("\n");
					}
					jProgressBar.setValue(100);
				}
			}
		}

		/**
		 * 压缩文件
		 * @param zout zip格式的文件输出流
		 * @param sourceFile 源文件路径（将要压缩的文件）
		 * @param zipEntryName 压缩条目（实际上就是一个文件的相对路径）
		 */
		private void zipFile(ZipOutputStream zout, File sourceFile, String zipEntryName)
				throws IOException, FileNotFoundException {
			sb.append(zipEntryName).append("\n");
			// 将一个将要压缩的文件写入到压缩条目中
			zout.putNextEntry(new ZipEntry(zipEntryName));
			// 读入将要压缩的文件
			FileInputStream fin = new FileInputStream(sourceFile);
			// 初始化变量值
			totalByte = fin.available();
			total = 0;
			currentValue = 0;
			// 将原文件写入到zip格式输出流
			byte[] buff = new byte[1024];
			int length;
			while ((length = fin.read(buff)) != -1) {
				zout.write(buff, 0, length);
				total += length;
				currentValue = (int) (total * 100 / totalByte);
				jProgressBar.setValue(currentValue);
			}
			jProgressBar.setValue(100);
			fin.close();
			zout.closeEntry();
		}
	}
}
