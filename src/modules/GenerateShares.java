package modules;

import gui.VCSMain;
import gui.VCSMainFrame;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GenerateShares {

	int totalShareCount = 2;
	int minShareCount	= 2;

	BufferedImage	secImage;
	VCSMainFrame	mainFrame;

	int[][] shareMatrix_c0;
	int[][] shareMatrix_c1;

	ArrayList<BufferedImage> imageList = new ArrayList<BufferedImage>();

	public GenerateShares(int k, int n, BufferedImage imageIn, VCSMainFrame frameIn) {
		totalShareCount = n;
		minShareCount = k;

		secImage = imageIn;
		mainFrame = frameIn;
	}

	public void process() {
		createShareMatrixes(minShareCount, totalShareCount);
		int w = secImage.getWidth();
		int h = secImage.getHeight();

		for (int l=0; l < totalShareCount; l++){
			imageList.add(new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY));
		}

		BufferedImage br_thd = getThresholdImage(secImage);
		mainFrame.getImageMap(VCSMain.KEY_THRESHOLD).put(VCSMain.KEY_THRESHOLD, br_thd);

		for (int i=0; i < w; i++) {
			for (int j=0; j < h; j++) {
				int pixelValue = br_thd.getRGB(i, j);
				int pattern[] = getRandomPattern(pixelValue);
				//System.out.println(pattern[0] + " " + pattern[1]);
				for (int count=0; count < imageList.size(); count++) {
					imageList.get(count).setRGB(i, j, getRGBValue(pattern[count]));
				}
			}
		}

		int count = 1;
		HashMap<String, BufferedImage> shareImageMap = mainFrame.getImageMap(VCSMain.KEY_SHARE);
		for (BufferedImage br: imageList) {
			shareImageMap.put(VCSMain.KEY_SHARE+count, br);
			count++;
		}
	}

	private int[] getRandomPattern(int pixelValue) {
		int rowLen = shareMatrix_c0.length;
		int colLen = shareMatrix_c0[0].length;
		int[] retValue = new int[rowLen];
		Random random = new Random();
		int randomIndex = random.nextInt(colLen);
		if (pixelValue == VCSMain.RGB_WHITE) {
			for (int i=0; i < rowLen; i++) {
				retValue[i] = shareMatrix_c0[i][randomIndex];
			}
		} else if (pixelValue == VCSMain.RGB_BLACK) {
			for (int i=0; i < rowLen; i++) {
				retValue[i] = shareMatrix_c1[i][randomIndex];
			}
		}

		//System.out.println("Pixel = " + pixelValue + " Random Pattern = " + retValue[0] + " " + retValue[1] + " " + retValue[2]);
		return retValue;
	}

	private int getRGBValue(int i) {
		if (i == 1)
			return VCSMain.RGB_WHITE;
		else
			return VCSMain.RGB_BLACK;
	}

	private BufferedImage getThresholdImage(BufferedImage brIn) {
		int width 	= brIn.getWidth();
		int height 	= brIn.getHeight();
		BufferedImage outImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
		outImage.createGraphics().drawImage(brIn, 0, 0, null);

		return outImage;
	}

	private void createShareMatrixes(int k, int n) {

		switch(k) {
		case 2:
			shareMatrix_c0 = new int[n][n];
			shareMatrix_c1 = new int[n][n];

			for (int i=0; i < n; i++) {
				for (int j=0; j < n; j++) {
					if (j == 0)
						shareMatrix_c0[i][j] = 1;
					else
						shareMatrix_c0[i][j] = 0;

					if (j == i)
						shareMatrix_c1[i][j] = 1;
					else
						shareMatrix_c1[i][j] = 0;
				}
			}
			break;

		case 3:
			//shareMatrix_c0 = new int[][]{{0, 1, 1, 0}, {0, 1, 0, 1}, {0, 0, 1, 1}};
			//shareMatrix_c1 = new int[][]{{0, 0, 1, 1}, {0, 1, 0, 1}, {1, 0, 0, 1}};
			
			int cols = 2*n -2;

			shareMatrix_c0 = new int[n][cols];
			shareMatrix_c1 = new int[n][cols];

			int[][] tempMatrix_B = new int[n][n-2];
			for (int p=0; p < n; p++) {
				for (int q=0; q < n-2; q++) {
					tempMatrix_B[p][q] = 1;
				}
			}

			int[][] tempMatrix_I = new int[n][n];
			for (int a=0; a < n; a++) {
				for (int b=0; b < n; b++) {
					if (b == a)
						tempMatrix_I[a][b] = 1;
					else
						tempMatrix_I[a][b] = 0;
				}
			}

			for (int d=0; d < n; d++) {
				int[] arr_B = tempMatrix_B[d];
				int[] arr_I = tempMatrix_I[d];

				int aLen = arr_B.length;
				int bLen = arr_I.length;
				System.arraycopy(arr_B, 0, shareMatrix_c1[d], 0, aLen);
				System.arraycopy(arr_I, 0, shareMatrix_c1[d], aLen, bLen);
			}

			for (int i=0; i < n; i++) {
				for (int j=0; j < cols; j++) {
					shareMatrix_c0[i][j] = (shareMatrix_c1[i][j] == 0)?1:0;
				}
			}
			break;
		}
		
		System.out.print("[");
		for (int i=0; i < shareMatrix_c0.length; i++) {
			int[] tempArr = shareMatrix_c0[i];
			System.out.print("[");
			for (int j=0; j < tempArr.length; j++) {
				System.out.print(tempArr[j]);
			}
			System.out.println("]");
		}
		System.out.println("]");
		
		System.out.print("[");
		for (int i=0; i < shareMatrix_c1.length; i++) {
			int[] tempArr = shareMatrix_c1[i];
			System.out.print("[");
			for (int j=0; j < tempArr.length; j++) {
				System.out.print(tempArr[j]);
			}
			System.out.println("]");
		}
		System.out.println("]");
	}
}
