package utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

public class ImageGenerator {
	public String imgCode;
	public BufferedImage image;

	private static Color getRandColor(int fc, int bc) {
		Random random = new Random();
		if (fc > 255)
			fc = 255;
		if (bc > 255)
			bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}

	private static String charsLong = "23456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ";

	private static String chars = charsLong;

	public static ImageGenerator make() {
		int charsLength = chars.length();
		int width = 68, height = 21;
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);

		Graphics g = image.getGraphics();

		Random random = new Random();

		g.setColor(getRandColor(200, 250));
		g.fillRect(0, 0, width, height);

		g.setFont(new Font("Times New Roman", Font.ITALIC, height - 3));

		g.setColor(getRandColor(160, 200));
		for (int i = 0; i < 35; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int xl = random.nextInt(12);
			int yl = random.nextInt(12);
			g.drawLine(x, y, x + xl, y + yl);
		}

		StringBuilder sRand = new StringBuilder();
		String[] fontNames = { "Century Gothic", "Arial", "Book antiqua", "" };

		for (int i = 0; i < 4; i++) {
			g.setFont(new Font(fontNames[random.nextInt(3)], Font.ITALIC,
					height - 3));
			char rand = chars.charAt(random.nextInt(charsLength));
			sRand.append(rand);

			g.setColor(new Color(20 + random.nextInt(110), 20 + random
					.nextInt(110), 20 + random.nextInt(110)));
			g.drawString(String.valueOf(rand), 16 * i + random.nextInt(5) + 4,
					height - 5 - random.nextInt(2));
		}

		g.dispose();

		// 生成图片完毕
		ImageGenerator result = new ImageGenerator();
		result.imgCode = sRand.toString();
		result.image = image;
		return result;
	}

	//按字节输出
	public byte[] getImgBytes() {

		ByteArrayOutputStream bot = new ByteArrayOutputStream();

		byte[] b = null;
		try {
			ImageIO.write(image, "png", bot);
			b = bot.toByteArray();
			bot.close();
		} catch (Exception e) {
		}
		return b;
	}

}
