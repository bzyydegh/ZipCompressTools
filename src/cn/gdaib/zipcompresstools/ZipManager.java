package cn.gdaib.zipcompresstools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 压缩部分
 * @Author:zhuyy
 * 创建于2016-02-26
 */
public class ZipManager {
	private StringBuffer sb;
	/**
	 * 压缩文件或文件夹
	 * @param sourcePath 源文件路径（将要压缩的文件）
	 * @param outPath 压缩条目（实际上就是一个文件的相对路径）
	 */
	public String zip(String sourcePath, String outPath) {
		try {
			sb = new StringBuffer("压缩文件如下：\n");
			//文件输出流
			FileOutputStream fout = new FileOutputStream(outPath);
			//写入数据校验和的输出流。校验和可用于验证输出数据的完整性
			CheckedOutputStream checkedOutputStream = new CheckedOutputStream(fout, new Adler32());			
			//zip格式的输出流
			ZipOutputStream zout = new ZipOutputStream(fout);
			//要将一个源文件写入到一个压缩文件中
			File sourceFile = new File(sourcePath);
			//压缩条目
			String zipEntryName = sourceFile.getName();
			
			if (sourceFile.isFile()){
				//压缩文件
				zipFile(zout, sourceFile, zipEntryName);
			}else{
				//压缩文件夹
				zipDirectory(zout, sourceFile, zipEntryName);
			}
			
			zout.close();
			//System.out.println("校验和为："+checkedOutputStream.getChecksum().getValue());
			sb.append("校验和为：" + checkedOutputStream.getChecksum().getValue()).append("\n");
			sb.append("压缩完成。");
			checkedOutputStream.close();
			fout.close();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 压缩文件夹
	 * @param zout zip格式的文件输出流
	 * @param sourceFile 源文件路径（将要压缩的文件）
	 * @param zipEntryName 压缩条目（实际上就是一个文件的相对路径）
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void zipDirectory(ZipOutputStream zout, File sourceFile, String zipEntryName)
			throws IOException, FileNotFoundException {
		//压缩目录，遍历目录里的所有文件
		for(File file:sourceFile.listFiles()){
			if(file.isFile()){
				//如果是文件，则直接调用压缩文件的方法进行压缩
				zipFile(zout, file, zipEntryName+"/"+file.getName());
			}else{
				//说明现在的file是目录，则需要将该目录的所有文件压缩
				if(file.listFiles().length > 0){
					//递归调用压缩文件夹的方法
					zipDirectory(zout, file, zipEntryName+"/"+file.getName());//资料/文档
				}else{
					//空文件夹
					//将压缩条目写入到压缩对象中
					zout.putNextEntry(new ZipEntry(zipEntryName+"/"+file.getName()+"/"));
					zout.closeEntry();
					sb.append(zipEntryName + "/" + file.getName() + "/").append("\n");
				}
				
			}
		}
	}
	
	/**
	 * 压缩文件
	 * @param zout zip格式的文件输出流
	 * @param sourceFile 源文件路径（将要压缩的文件）
	 * @param zipEntryName 压缩条目（实际上就是一个文件的相对路径）
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void zipFile(ZipOutputStream zout, File sourceFile, String zipEntryName)
			throws IOException, FileNotFoundException {
		sb.append(zipEntryName).append("\n");
		//将一个将要压缩的文件写入到压缩条目中
		zout.putNextEntry(new ZipEntry(zipEntryName));
		//读入将要压缩的文件
		FileInputStream fin = new FileInputStream(sourceFile);
		//将原文件写入到zip格式输出流
		byte[] buff = new byte[1024];
		int length;
		while((length = fin.read(buff)) != -1){
			zout.write(buff, 0, length);
		}
		fin.close();
		zout.closeEntry();
	}
}
