package cn.gdaib.zipcompresstools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 解压部分
 * @Author:zhuyy
 * 创建于2016-02-26
 */
public class UnzipManager {
	/**
	 * 解压文件或文件夹
	 * @param sourcePath 源文件路径（将要压缩的文件夹）
	 * @param outPath 输出文件的路径
	 */
	public String unZip(String sourcePath, String outPath) {
		try {
			StringBuffer sb = new StringBuffer("解压文件如下：\n");
			//文件输入流
			FileInputStream fin = new FileInputStream(sourcePath);
			//所读取数据校验和的输入流。校验和可用于验证输入数据的完整性
			CheckedInputStream checkedInputStream = new CheckedInputStream(fin, new Adler32());
			//zip格式的输入流
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry zipEntry;
			//遍历压缩文件中的所有压缩条目
			while((zipEntry = zin.getNextEntry()) != null){
				sb.append(zipEntry.getName()).append("\n");
				//将压缩条目输出到输出路径
				File targetFile = new File(outPath + File.separator + zipEntry.getName());
				//判断目标输出文件的父文件夹是否存在，如果不存在的话则需要创建一个文件夹
				if(!targetFile.getParentFile().exists()){
					//不存在父文件夹
					targetFile.getParentFile().mkdirs();
				}
				if(zipEntry.isDirectory()){
					//判断当前的压缩条目是否是文件夹，如果是文件夹的话，直接在输出路径创建文件即可
					targetFile.mkdirs();	
				}else{
					//当前的压缩条目是一个文件，需要将输入的压缩文件内容输出到输出路径
					FileOutputStream fout = new FileOutputStream(targetFile);
					byte[] buff = new byte[1024];
					int length;
					while((length = zin.read(buff)) != -1){
						fout.write(buff, 0, length);
					}
					fout.close();
				}
			}
			zin.close();
			//System.out.println("校验和为："+checkedInputStream.getChecksum().getValue());
			sb.append("校验和为：" + checkedInputStream.getChecksum().getValue()).append("\n");
			sb.append("解压结束");
			checkedInputStream.close();
			fin.close();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
