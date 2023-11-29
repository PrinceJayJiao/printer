package com.jay.listener;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;

import java.nio.file.Path;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.math.BigInteger;


public class AnalyzeActionFile extends JFrame  {
	static int g_prv_pos = 0;
	static int g_cur_pos = 0;
	static XWPFDocument document = new XWPFDocument();
	static boolean isFixedValueFile = false;
	static String filePath;

	public static void main(String args[]) {
		filePath = args[0];

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			// 50M缓冲区
			char[] buffer = new char[50 * 1024 * 1024];
			// 读取所有数据到缓冲区
			int totalLength = reader.read(buffer);

			analyzeActionFile(buffer, totalLength);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getDocumentName(String filename) {
		// 从文件路径中提取文件名（不包含后缀）
		String[] parts = filename.split("/");
		String fileNameWithExtension = parts[parts.length - 1];
		int dotIndex = fileNameWithExtension.lastIndexOf('.');
		if (dotIndex == -1) {
			// 文件名中没有后缀
			return fileNameWithExtension;
		} else {
			// 去掉文件后缀
			return fileNameWithExtension.substring(0, dotIndex);
		}
	}


	private static void insertImage(XWPFDocument document, String imagePath, int width) {
		try {
			// 添加图片
			XWPFParagraph paragraph = document.createParagraph();
			XWPFRun run = paragraph.createRun();

			// 获取原始图片的宽度和高度
			FileInputStream imageStream = new FileInputStream(imagePath);
			BufferedImage bufferedImage = ImageIO.read(imageStream);
			int originalWidth = bufferedImage.getWidth();
			int originalHeight = bufferedImage.getHeight();

			// 计算新的高度，确保宽度与文档一样宽
			int newHeight = (int) Math.round((double) originalHeight / originalWidth * width);

			// 设置图片宽度和高度
			run.addPicture(new FileInputStream(imagePath), XWPFDocument.PICTURE_TYPE_PNG, imagePath,
					Units.toEMU(width), Units.toEMU(newHeight));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void analyzeActionFile(char[] buffer, int totalLength)  {

		int pos = 0;
		boolean find0D0A = false;
		boolean find1B44 = false;
		int i = 0, j = 0;
		int picCount = 0;
		//System.out.print("totalen : " + totalLength + "\n");
		while(pos < (totalLength - 4)){
			if(buffer[pos] == '1' && buffer[pos + 1] == 'B'  && buffer[pos + 3] == '2'  && buffer[pos + 4] == 'A' )
			{
				j = pos;
				find0D0A = false;
				while(find0D0A == false) {
					if(buffer[j - 1] == '0' && buffer[j] == 'D')
						find0D0A = true;
					j--;
				}
				find0D0A = false;
				while(find0D0A == false) {
					if(buffer[j - 1] == '0' && buffer[j] == 'D')
						find0D0A = true;
					j--;
				}

				StringBuilder vstring = new StringBuilder(j - g_prv_pos);
				for (i = g_prv_pos; i < j; i++) {
					//System.out.print(buffer[j]);
					vstring.append(buffer[i]);
				}
				//System.out.println("\n");
				String vresultString = vstring.toString();
				//System.out.println(vresultString);
				String vhresultString = decodeHexwithGB2312(vresultString);
				//System.out.println(vhresultString);

				// 添加文字
				// 将字符串分解为多个段落
				String[] paragraphs = vhresultString.split("\n");

				// 创建每个段落
				for (String paragraphText : paragraphs) {
					XWPFParagraph paragraph = document.createParagraph();
					XWPFRun run = paragraph.createRun();
					run.setText(paragraphText);
					run.addBreak();
				}

				picCount++;
				searchAndGenPic(buffer,totalLength,picCount, pos);
				pos = g_prv_pos;
			}
			else {
				pos = pos + 1;
				if(pos == (totalLength - 4)) {
					//说明整个文件不存在1B2A，则直接调用解析程序解析原来的_gb2312文件
					if(g_prv_pos == 0){

						isFixedValueFile = true;

					}else{


						//如果是最后一段文本，则写入文本后保存文件
						StringBuilder vstring = new StringBuilder(totalLength  - g_prv_pos);


						for (i = g_prv_pos; i < totalLength; i++) {
							//System.out.print(buffer[j]);
							vstring.append(buffer[i]);
						}
						//System.out.println("\n");
						String vresultString = vstring.toString();
						//System.out.println(vresultString);
						String vhresultString = decodeHexwithGB2312(vresultString);



						// 添加文字
						// 将字符串分解为多个段落
						String[] paragraphs = vhresultString.split("\n");

						// 创建每个段落
						for (String paragraphText : paragraphs) {
							XWPFParagraph paragraph = document.createParagraph();
							XWPFRun run = paragraph.createRun();
							run.setText(paragraphText);
							run.addBreak();
						}



						String documentPath = "/home/linaro/rcv/actionFile";
						String documentName = getDocumentName(filePath);
						String fullPath = Paths.get(documentPath, documentName+".docx").toString();
						System.out.println(fullPath);

						// 判断目录是否存在
						boolean isDirectoryExist = Files.exists(Paths.get(documentPath));

						if (!isDirectoryExist) {
							// 如果目录不存在，则创建目录
							try {
								Files.createDirectories(Paths.get(documentPath));
							} catch (IOException e) {
								e.printStackTrace();
							}

						}
						// 保存文档
						System.out.println("successfully");
						try (FileOutputStream fos = new FileOutputStream(fullPath)) {
							document.write(fos);
							System.out.println("Word document created successfully.");
						}catch (IOException e) {
							e.printStackTrace();
						}



						System.out.println("end");
					}
				}

			}

		}

	}







	private static boolean isHexCharacter(char c) {
		return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
	}

	private static int hexCharToInt(char hexChar) {
		// 将字符转换为大写，以处理小写字母
		hexChar = Character.toUpperCase(hexChar);

		// 如果是数字字符，直接转换为整数 )
		if (hexChar >= '0' && hexChar <= '9') {
			return hexChar - '0';
		}
		// 如果是字母字符(A-F)，转换为对应的整数值
		else if (hexChar >= 'A' && hexChar <= 'F') {
			return hexChar - 'A' + 10;
		}
		// 非法字符返回-1或者其他你认为合适的值
		else {
			return -1;
		}
	}

	private static String hexCharToBinary(char hexChar) {
		// 将十六进制字符转换为整数
		int intValue = Character.digit(hexChar, 16);

		// 使用Integer.toBinaryString将整数转换为二进制字符串
		return String.format("%4s", Integer.toBinaryString(intValue)).replace(' ', '0');
	}

	// 判断二进制表示的某一位是否为1
	private static boolean isBitSet(String binaryString, int bitIndex) {
		// 从右到左计算索引，因此需要反转字符串
		String reversedBinaryString = new StringBuilder(binaryString).reverse().toString();

		// 判断指定位的值是否为1
		return reversedBinaryString.charAt(bitIndex) == '1';
	}


	private static String decodeHex(String hexString) {
		// 移除 "[HEX]" 和换行符
		hexString = hexString.replace("[HEX]", "").replaceAll("\n", "");

		StringBuilder decodedStringBuilder = new StringBuilder();

		// 移除可能存在的空格
		hexString = hexString.replaceAll(" ", "");

		try {
			for (int i = 0; i < hexString.length(); i += 2) {
				String hexPair = hexString.substring(i, i + 2);
				int decimalValue = Integer.parseInt(hexPair, 16);
				decodedStringBuilder.append((char) decimalValue);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		return decodedStringBuilder.toString();
	}


	private static String decodeHexwithGB2312(String hexString) {
		// 移除 "[HEX]"
		hexString = hexString.replace("[HEX]", "");
		hexString = hexString.replace("\n", "");
		StringBuilder decodedStringBuilder = new StringBuilder();

		// 移除可能存在的空格
		hexString = hexString.replaceAll(" ", "");

		try {
			byte[] bytes = new byte[hexString.length() / 2];
			for (int i = 0; i < hexString.length(); i += 2) {
				bytes[i / 2] = (byte) Integer.parseInt(hexString.substring(i, i + 2), 16);
			}
			decodedStringBuilder.append(new String(bytes, "GB2312"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return decodedStringBuilder.toString();
	}


	// 提取字符串中的字母、数字和空格到数组
	private static char[] extractCharacters(String inputString) {
		char[] charactersArray = inputString.replaceAll("[^a-zA-Z0-9\\s]", "").toCharArray();
		return charactersArray;
	}

	private static void drawTickLabel(Graphics2D g, String label, int x, int y) {
		g.setColor(Color.BLACK);
		g.drawString(label, x, y);
	}


	private static void generatePicture(int row, int datasearchlength, int lcount, String dhresultString, ArrayList<String> dLArray,int[][] Matrix,int picCount) {
		int matrixWidth = datasearchlength;
		int matrixHeight = row + 24;
		int emptycount = 0;
		int hpos1 = 0;
		int hpos2 = 0;
		int i = 0, j = 0;
		boolean findempty = false;
		String s1 = " ";
		BufferedImage image = new BufferedImage(matrixWidth + 50, matrixHeight + 50, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		//j = pos
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, matrixWidth + 50, matrixHeight + 50);

		// hlen = dhresultString.length();
		char[] hcharray  = extractCharacters(dhresultString);

		int harlen = hcharray.length;

		for (i = 5; i < harlen; i++)
			if((hcharray[i] >= '1' && hcharray[i] <= '9') || (hcharray[i] >= 'a' && hcharray[i] <= 'z')
					||(hcharray[i] >= 'A' && hcharray[i] <= 'Z')) {
				hpos1 = i;
				while(hcharray[i] == ' ' && i>0) {
					i--;
					emptycount++;
				}
				break;
			}


		for(i = harlen - 1; i >0; i--)
			if(hcharray[i] != ' ') {
				hpos2 = i + 1;
				break;
			}

		int xTickSpacing = matrixWidth / (hpos2 - hpos1);
		int yTickSpacing = matrixHeight / lcount ;
		for(i = 0; i < matrixHeight; i++) {
			for( j = 0; j < matrixWidth; j++) {
				Color color = (Matrix[i][j] == 1) ? Color.BLACK : Color.WHITE;
				g.setColor(color);
				// g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
				g.drawLine(j + 40, i + 20, j + 40, i + 20);
			}
		}

		if(hpos1 > 0) {
			s1 = dhresultString.substring(0, hpos1);
			drawTickLabel(g, s1, 0, 10);
		}
		// 绘制横轴刻度
		for ( i = hpos1; i < hpos2; i++) {
			int x = 0;
			if(emptycount > 10)
				x = (i-hpos1 + 1) * xTickSpacing + 40;
			else
				x = (i-hpos1) * xTickSpacing + 40;
			int y = 10;
			drawTickLabel(g, Character.toString(hcharray[i]), x, y);
		}

		// 绘制竖轴刻度
		for ( i = 0; i < lcount; i++) {
			int x = 0;
			int y = (i + 1) * yTickSpacing + 20;
			s1 = dLArray.get(i);
			drawTickLabel(g, s1, x, y);
		}


		g.dispose();

		// 将图像保存为图片文件
		try {
			// 替换此路径为你希望保存图片的路径
			String picturePath = "pictures";
			boolean isDirectoryExist = Files.exists(Paths.get(picturePath));
			if (!isDirectoryExist) {
				// 如果目录不存在，则创建目录
				Files.createDirectories(Paths.get(picturePath));
			}
			s1 = Integer.toString(picCount);
			String fullPath = Paths.get(picturePath, s1 + ".png").toString();

			File output = new File(fullPath);
			ImageIO.write(image, "png", output);
			System.out.println("图片已保存到 " + output.getAbsolutePath());
			// 添加图片
			insertImage(document, output.getAbsolutePath(), 400);
			if (output.exists()) {
				if (output.delete()) {
					System.out.println("文件删除成功");
				} else {
					System.out.println("文件删除失败");
				}
			} else {
				System.out.println("文件不存在");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	enum State {
		s_start,s_datacontstart,s_datacontmore,s_newdata,s_search;
	}

	private static void searchAndGenPic(char[] buffer,int totalLength,int picCount, int pos) {
		int[][] Matrix;



		State st = State.s_start;
		State prv_st = State.s_start;
		//int pos = 0;
		int prv_pos = 0;
		int datalen = 0;
		int datasearchlength = 0;
		int n1 = 0;
		int n2 = 0;
		int v1 = 0;
		int v2 = 0;
		int i = 0;
		int j = 0;
		int row = -24;
		int col = 0;
		int k = 0;
		int m = 0;
		int n = 0;
		int clen = 0;
		int hlen = 0;
		int harlen = 0;
		int llen = 0;
		boolean isBitSet = false;
		boolean hasn = false;
		boolean hashex = false;
		boolean paint = false;
		boolean find0D0A= false;
		boolean findZeroNine = false;
		boolean find1C26 = false;
		boolean find1B = false;
		//boolean hasr = false;
		int hexpos = 0;
		int npos = 0;
		int hpos1 = 0;
		int hpos2 = 0;
		int lpos1 = 0;
		int lpos2 = 0;
		ArrayList<String> dLArray = new ArrayList<>();
		String s1;
		String dhresultString = " ";
		int vpos = 0;
		// int rpos = 0;
		//int cellsize = 1;
		int count = 0;
		int lcount = 0;
		int emptycount = 0;
		int matrixWidth = 0;
		int matrixHeight = 0;
		String binaryString1 = " ";
		String binaryString2 = " ";
		String binaryString3 = " ";
		String binaryString4 = " ";
		String binaryString5 = " ";
		String binaryString6 = " ";
		Matrix = new int[10000][3000];

		for (j = 0; j < 10000; j++)
			for (k = 0; k< 3000; k++)
				Matrix[j][k] = 0;

		while((pos < (totalLength - 4)) && (st !=  State.s_newdata)) {
			if ((st ==  State.s_start || st ==  State.s_datacontstart || st ==  State.s_datacontmore ) && buffer[pos] == '1' && buffer[pos + 1] == 'B'  && buffer[pos + 3] == '2'  && buffer[pos + 4] == 'A' )
			{     pos = pos + 6;
				//for ( j = 0; j< 9; j++)
				//System.out.print(buffer[pos + j]);
				count = count + 1;
				hasn = false;
				hashex = false;
				for ( j = 0; j < 9; j++) {
					if(buffer[pos + j] == '\n') {
						hasn = true;
						npos = j;
						// System.out.println("n : "+ st + "    npos :  " + npos + "\n");
					}
					if(buffer[pos + j] == '[' && buffer[pos + 1 + j] == 'H' && buffer[pos + 2 + j] == 'E' && buffer[pos + 3 + j] == 'X' && buffer[pos + 4 + j] == ']' )
					{ hashex = true;
						hexpos = j;
						//System.out.println("[HEX] : "+ st + "    hexpos :  " + hexpos + "\n");
					}
				}
				if(hashex == false) {
					//System.out.println("st ==  " + st + "\n");
					v1 = hexCharToInt(buffer[pos + 3]);
					v2 = hexCharToInt(buffer[pos + 4]);
					n1 = v1 * 16 + v2;
					v1 = hexCharToInt(buffer[pos + 6]);
					v2 = hexCharToInt(buffer[pos + 7]);
					n2 = v1 * 16 + v2;
					if  (buffer[pos] == '0')
						datalen = n1 + n2 * 256;
					else
						datalen = 3*(n1 + n2 * 256);
					pos = pos + 9;
				}
				else {
					if(hexpos == 0) {
						pos = pos + 6;
						v1 = hexCharToInt(buffer[pos + 3]);
						v2 = hexCharToInt(buffer[pos + 4]);
						n1 = v1 * 16 + v2;
						v1 = hexCharToInt(buffer[pos + 6]);
						v2 = hexCharToInt(buffer[pos + 7]);
						n2 = v1 * 16 + v2;
						if  (buffer[pos] == '0')
							datalen = n1 + n2 * 256;
						else
							datalen = 3*(n1 + n2 * 256);
						pos = pos + 9;
					}
					else if(hexpos == 3) {
						pos = pos + 9;
						v1 = hexCharToInt(buffer[pos + 0]);
						v2 = hexCharToInt(buffer[pos + 1]);
						n1 = v1 * 16 + v2;
						v1 = hexCharToInt(buffer[pos + 3]);
						v2 = hexCharToInt(buffer[pos + 4]);
						n2 = v1 * 16 + v2;
						if  (buffer[pos - 9] == '0')
							datalen = n1 + n2 * 256;
						else
							datalen = 3*(n1 + n2 * 256);
						pos = pos + 6;
					}
					else if(hexpos == 6) {
						v1 = hexCharToInt(buffer[pos + 3]);
						v2 = hexCharToInt(buffer[pos + 4]);
						n1 = v1 * 16 + v2;
						pos = pos + 12;
						v1 = hexCharToInt(buffer[pos + 0]);
						v2 = hexCharToInt(buffer[pos + 1]);
						n2 = v1 * 16 + v2;
						if  (buffer[pos - 12] == '0')
							datalen = n1 + n2 * 256;
						else
							datalen = 3*(n1 + n2 * 256);
						pos = pos + 3;
					}
				}
				//System.out.println("n1: " + n1 + " n2: " + n2+ " datalen:  "  + datalen +"\n");
				//if(hashex == true){
				//    for(i = pos - 15; i < pos + 50; i++)
				//      System.out.print(buffer[i]);
				//    System.out.print("\n");
				// }
				//System.out.println("prv_pos3 : "  + prv_pos + " pos3 : " + pos + " row  " + row + " count : " + count + "\n");
				prv_st = st;
				if(st == State.s_start) {
					st =  State.s_datacontstart;
					j = pos - 16;
					find0D0A = false;
					while(find0D0A == false) {
						if(buffer[j - 1] == '0' && buffer[j] == 'D'){
							hpos2 = j - 2;
							find0D0A = true;
							find1C26 = false;
							k = j + 5;
							while(find1C26 == false && k < pos)
								if(buffer[k] == '1' && buffer[k + 1] == 'C' && buffer[k + 3] == '2' && buffer[k + 4] == '6') {
									find1C26 = true;
									k = k + 6;
									m = k;
								}
								else    	k++;
							if(find1C26 == true) {
								find1B = false;
								while(find1B == false)
									if(buffer[m] == '1' && buffer[m + 1] == 'B') {
										find1B = true;
										StringBuilder vstring = new StringBuilder(m - k);
										for (n = k; n < m; n++) {
											//System.out.print(buffer[j]);
											vstring.append(buffer[n]);
										}
										//System.out.println("\n");
										String vresultString = vstring.toString();
										String vhresultString = decodeHex(vresultString);
										dLArray.add(vhresultString);
										lcount = lcount + 1;
										//System.out.println("count : " + count +" Decoded:" + vhresultString);
										//System.out.println("\n");
									}
									else 	m++;
							}
							else {
								dLArray.add(" ");
								lcount = lcount + 1;
								//System.out.println("count : " + count);
							}
						}
						else {  j--;
						}
					}
					find0D0A = false;
					while(find0D0A == false) {
						if(buffer[j - 1] == '0' && buffer[j] == 'A'){
							findZeroNine = false;
							while(findZeroNine == false ) {
								if(buffer[j - 1] == '0' && buffer[j] == '9'){
									hpos1 = j + 2;
									findZeroNine = true;
								}
								else {
									j++;
								}
							}
							find0D0A = true;
						}
						else {  j--;
						}
					}
					StringBuilder hstring = new StringBuilder(hpos2 - hpos1);
					for (j = hpos1; j < hpos2; j++) {
						//System.out.print(buffer[j]);
						hstring.append(buffer[j]);
					}
					//System.out.println("\n");
					String hresultString = hstring.toString();
					dhresultString = decodeHex(hresultString);
					//System.out.println("Decoded: " + dhresultString);


					// 打印数组及大小
					// System.out.println("Characters Array: " + new String(hcharray));
					//System.out.println("Array Size: " + hcharray.length);

				}
				else {
					if(pos - prv_pos < 200) {
						st = State.s_datacontmore;
						//System.out.println(" pos - prv_pos < 100 st = State.s_datacontmore\n"  + pos+"\n");
						j = pos - 16;
						find0D0A = false;
						while(find0D0A == false) {
							if(buffer[j - 1] == '0' && buffer[j] == 'D'){
								hpos2 = j - 2;
								find0D0A = true;
								find1C26 = false;
								k = j + 5;
								while(find1C26 == false && k < pos)
									if(buffer[k] == '1' && buffer[k + 1] == 'C' && buffer[k + 3] == '2' && buffer[k + 4] == '6') {
										find1C26 = true;
										k = k + 6;
										m = k;
									}
									else    	k++;
								if(find1C26 == true) {
									find1B = false;
									while(find1B == false)
										if(buffer[m] == '1' && buffer[m + 1] == 'B') {
											find1B = true;
											StringBuilder vstring = new StringBuilder(m - k);
											for (n = k; n < m; n++) {
												//System.out.print(buffer[j]);
												vstring.append(buffer[n]);
											}
											//System.out.println("\n");
											String vresultString = vstring.toString();
											String vhresultString = decodeHex(vresultString);
											dLArray.add(vhresultString);
											lcount = lcount + 1;
											//System.out.println("count : " + count + " Decoded:" + vhresultString);
										}
										else 	m++;
								}
								else {
									dLArray.add(" ");
									lcount = lcount + 1;
									//System.out.println("count : " + count);
								}
							}
							else {  j--;
							}
						}
					}
					else {
						generatePicture(row, datasearchlength, lcount,dhresultString, dLArray, Matrix, picCount);
						st =  State.s_newdata;
						g_prv_pos = prv_pos;
						g_cur_pos = pos;
						//System.out.println("matrixHeight : " + matrixHeight + " matrixWidth : " + matrixWidth + "\n");
						//System.out.println("st =  State.s_newdata \npos:"  + pos+"\n");
						//System.out.println("count :"  + count+"\n");
						//System.out.println("row :"  + row+"\n");
						//SwingUtilities.invokeLater(() -> new MatrixDrawer(Matrix));
					}
				}
				//System.out.println("st ==  State.s_datacontstart " + pos+"\n");
				row = row + 24;
				//System.out.println("count :"  + count+" row :"  + row +"\n");
			}
			else {
				pos = pos + 1;
				g_prv_pos = prv_pos;
				g_cur_pos = pos;
				prv_st = State.s_search;
				if( pos == (totalLength - 4)) {
					generatePicture(row, datasearchlength, lcount,dhresultString, dLArray, Matrix, picCount);
					st =  State.s_newdata;
					//System.out.println("matrixHeight : " + matrixHeight + " matrixWidth : " + matrixWidth + "\n");
					//System.out.println("totallength : " + totallength + " npos:"  + pos+"\n");
				}
			}

			if( (st ==  State.s_datacontmore || st ==  State.s_datacontstart) && (prv_st != State.s_search)) {
				//
				//if (count == 1) {
				//	System.out.println("count : " + count +"\n");
				//	System.out.println("row : " + row +"\n");
				//	for (k = 0; k < 100; k++)
				//	  System.out.print(buffer[pos + k]);
				//      System.out.println("\n");
				//}
				//System.out.println("state : "+ st + "    pos :  " + pos + "\n");
				datasearchlength = datalen/3;
				//System.out.println("datasearchlength : "+ datasearchlength + "\n");
				for (col  = 0; col < datasearchlength; col++) {
					// for (k = 0; k < 9; j++)

					hasn = false;
					hashex = false;
					for ( j = 0; j < 9; j++) {
						if(buffer[pos + j] == '\n') {
							hasn = true;
							npos = j;
							// System.out.println("n : "+ st + "    npos :  " + npos + "\n");
						}
						if(buffer[pos + j] == '[' && buffer[pos + 1 + j] == 'H' && buffer[pos + 2 + j] == 'E' && buffer[pos + 3 + j] == 'X' && buffer[pos + 4 + j] == ']' )
						{ hashex = true;
							hexpos = j;
							//System.out.println("[HEX] : "+ st + "    hexpos :  " + hexpos + "\n");
						}
					}
					if( hashex == false) {
						binaryString1 = hexCharToBinary(buffer[pos]);
						binaryString2 = hexCharToBinary(buffer[pos + 1]);
						binaryString3 = hexCharToBinary(buffer[pos + 3]);
						binaryString4 = hexCharToBinary(buffer[pos + 4]);
						binaryString5 = hexCharToBinary(buffer[pos + 6]);
						binaryString6 = hexCharToBinary(buffer[pos + 7]);
						pos = pos + 9;
					}
					if( hasn == true &&  hashex == true) {
						// else {
						if(npos ==  0) {
							//System.out.print(" npos == 0 \n");
							pos = pos + 7;
							binaryString1 = hexCharToBinary(buffer[pos]);
							binaryString2 = hexCharToBinary(buffer[pos + 1]);
							binaryString3 = hexCharToBinary(buffer[pos + 3]);
							binaryString4 = hexCharToBinary(buffer[pos + 4]);
							binaryString5 = hexCharToBinary(buffer[pos + 6]);
							binaryString6 = hexCharToBinary(buffer[pos + 7]);
							//  System.out.print(buffer[pos]);
							//   System.out.print(buffer[pos + 1]);
							//   System.out.print(buffer[pos + 2]);
							//    System.out.print(buffer[pos + 3]);
							// System.out.print(buffer[pos + 4]);
							//  System.out.print(buffer[pos + 5]);
							//  System.out.print(buffer[pos + 6]);
							//  System.out.print(buffer[pos + 7]);
							//   System.out.print(buffer[pos + 8]);
							//	 System.out.print("\n");
							pos = pos + 9;
							//System.out.println(buffer[pos] + buffer[pos+1] + buffer[pos+2] + buffer[pos+3] + buffer[pos+4] + buffer[pos+5] +  buffer[pos+6]+ buffer[pos+7]+ buffer[pos+8]+"\n");          			       pos = pos + 9;
						}
						if(npos ==  1) {
							binaryString1 = hexCharToBinary(buffer[pos]);
							//System.out.print(buffer[pos]);
							pos = pos + 7;
							binaryString2 = hexCharToBinary(buffer[pos + 0]);
							binaryString3 = hexCharToBinary(buffer[pos + 2]);
							binaryString4 = hexCharToBinary(buffer[pos + 3]);
							binaryString5 = hexCharToBinary(buffer[pos + 5]);
							binaryString6 = hexCharToBinary(buffer[pos + 6]);
							pos = pos + 9;
						}
						if(npos ==  2) {
							binaryString1 = hexCharToBinary(buffer[pos]);
							binaryString2 = hexCharToBinary(buffer[pos + 1]);
							pos = pos + 7;
							binaryString3 = hexCharToBinary(buffer[pos + 0]);
							binaryString4 = hexCharToBinary(buffer[pos + 1]);
							binaryString5 = hexCharToBinary(buffer[pos + 3]);
							binaryString6 = hexCharToBinary(buffer[pos + 4]);
							pos = pos + 9;
						}
						if(npos ==  3) {
							// System.out.print(" npos == 3 \n");
							binaryString1 = hexCharToBinary(buffer[pos]);
							binaryString2 = hexCharToBinary(buffer[pos + 1]);
							// System.out.print(buffer[pos]);
							// System.out.print(buffer[pos + 1]);
							//  System.out.print(' ');
							//System.out.print(buffer[pos] + buffer[pos + 1] + ' ');
							pos = pos + 10;
							binaryString3 = hexCharToBinary(buffer[pos + 0]);
							binaryString4 = hexCharToBinary(buffer[pos + 1]);
							binaryString5 = hexCharToBinary(buffer[pos + 3]);
							binaryString6 = hexCharToBinary(buffer[pos + 4]);
							// System.out.print(buffer[pos]);
							// System.out.print(buffer[pos + 1]);
							//  System.out.print(buffer[pos + 2]);
							////  System.out.print(buffer[pos + 3]);
							//  System.out.print(buffer[pos + 4]);
							//  System.out.print(buffer[pos + 5]);
							//   System.out.print("\n");
							//System.out.print(buffer[pos] + buffer[pos + 1] + buffer[pos + 2] + buffer[pos + 3] + buffer[pos + 4] + buffer[pos + 5] + "\n");
							pos = pos + 6;
						}
						if(npos ==  4) {
							binaryString1 = hexCharToBinary(buffer[pos]);
							binaryString2 = hexCharToBinary(buffer[pos + 1]);
							binaryString3 = hexCharToBinary(buffer[pos + 3]);
							pos = pos + 7 + 4;
							binaryString4 = hexCharToBinary(buffer[pos + 0]);
							binaryString5 = hexCharToBinary(buffer[pos + 2]);
							binaryString6 = hexCharToBinary(buffer[pos + 3]);
							pos = pos + 5;             			 }
						if(npos ==  5) {
							binaryString1 = hexCharToBinary(buffer[pos]);
							binaryString2 = hexCharToBinary(buffer[pos + 1]);
							binaryString3 = hexCharToBinary(buffer[pos + 3]);
							binaryString4 = hexCharToBinary(buffer[pos + 4]);
							pos = pos + 12;
							binaryString5 = hexCharToBinary(buffer[pos + 0]);
							binaryString6 = hexCharToBinary(buffer[pos + 1]);
							pos = pos + 4;
						}
						if(npos ==  6) {
							// System.out.print(" npos == 6 \n");
							binaryString1 = hexCharToBinary(buffer[pos]);
							binaryString2 = hexCharToBinary(buffer[pos + 1]);
							binaryString3 = hexCharToBinary(buffer[pos + 3]);
							binaryString4 = hexCharToBinary(buffer[pos + 4]);
							// System.out.print(buffer[pos]);
							// System.out.print(buffer[pos + 1]);
							//  System.out.print(buffer[pos + 2]);
							// System.out.print(buffer[pos + 3]);
							// System.out.print(buffer[pos + 4]);
							//System.out.println(buffer[pos] + buffer[pos + 1] + buffer[pos + 2] + buffer[pos + 3] + buffer[pos + 4] + ' ');
							pos = pos + 13;
							binaryString5 = hexCharToBinary(buffer[pos + 0]);
							binaryString6 = hexCharToBinary(buffer[pos + 1]);
							// System.out.print(' ');
							// System.out.print(buffer[pos]);
							// System.out.print(buffer[pos + 1]);
							//  System.out.print("\n");
							//System.out.println(buffer[pos] + buffer[pos + 1] + buffer[pos + 2] + "\n");
							pos = pos + 3;
						}
						if(npos ==  7) {
							binaryString1 = hexCharToBinary(buffer[pos]);
							binaryString2 = hexCharToBinary(buffer[pos + 1]);
							binaryString3 = hexCharToBinary(buffer[pos + 3]);
							binaryString4 = hexCharToBinary(buffer[pos + 4]);
							binaryString5 = hexCharToBinary(buffer[pos + 6]);
							pos = pos + 14;
							binaryString6 = hexCharToBinary(buffer[pos + 0]);
							pos = pos + 2;
						}
						if(npos ==  8) {
							binaryString1 = hexCharToBinary(buffer[pos]);
							binaryString2 = hexCharToBinary(buffer[pos + 1]);
							binaryString3 = hexCharToBinary(buffer[pos + 3]);
							binaryString4 = hexCharToBinary(buffer[pos + 4]);
							binaryString5 = hexCharToBinary(buffer[pos + 6]);
							binaryString6 = hexCharToBinary(buffer[pos + 7]);
							pos = pos + 16;
						}

					}
					//System.out.println(buffer[pos]+buffer[pos+1]+" "+buffer[pos+3]+buffer[pos+4] + " "+buffer[pos + 6]+buffer[pos + 7] + "\n");
					//if(row  < 200000)   {
					for ( j = 0; j < 24; j++) {
						k = j / 4;
						if( k == 0) {
							isBitSet = isBitSet(binaryString1, (3- (j%4)));

						}
						if (k == 1) {
							isBitSet = isBitSet(binaryString2, (3- (j%4)));
						}
						if( k == 2)
						{ isBitSet = isBitSet(binaryString3, (3- (j%4)));

						}
						if( k == 3) {
							isBitSet = isBitSet(binaryString4, (3-  (j%4)));
						}
						if ( k == 4) {
							isBitSet = isBitSet(binaryString5, (3- (j%4)));
						}
						if  ( k == 5) {
							isBitSet = isBitSet(binaryString6, (3 - (j%4)));
						}
						if(isBitSet == true) {
							Matrix[row + j][col] =  1;
							//if(count  == 1)  System.out.println("row : " + row + " j : " + j + " col : " + col +"\n");
							//count = count + 1;
						}
						//Matrix[row + j][col] =

					}

					//pos = pos + datalen;

				}
				//}
				// if(row > 20000 && paint == false)  {
				//   SwingUtilities.invokeLater(() -> new MatrixDrawer(Matrix));
				//	   paint = true;
				prv_pos = pos;
				//System.out.println("prv_pos1 : "  + prv_pos + " pos1 : " + pos + " row  " + row + " count : " + count + "\n");
				find0D0A = false;
				while((pos < totalLength) && (find0D0A == false)) {
					//while(find0D0A == false) {
					if((buffer[pos] == '0') && (buffer[pos + 1] == 'D')){
						find0D0A = true;
						pos = pos + 6;
					}
					else {
						prv_pos = pos;
						pos = pos + 1;
					}
				}
			}
		}
	}


}
