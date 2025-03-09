package mclachlan.maze.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 *
 */
public class AudioFormatDebug
{
	public static void main(String[] args)
	{
		File folder = new File("data/default/sound");
		if (!folder.isDirectory())
		{
			System.out.println("Invalid folder: " + args[0]);
			return;
		}

		// List all .ogg files in the folder
		File[] oggFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".ogg"));

		if (oggFiles == null || oggFiles.length == 0)
		{
			System.out.println("No OGG files found in: " + folder.getAbsolutePath());
			return;
		}

		// Print table header
	        System.out.printf("%-20s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s\n",
	                "File Name", "Encoding", "Sample Rate", "Channels", "Bit Depth", "Frame Rate", "Frame Size", "Big Endian", "Valid?");

	        System.out.println("----------------------------------------------------------------------------------------------------------");

		for (File oggFile : oggFiles) {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(oggFile))) {
            debugAudioFormat(oggFile.getName(), inputStream);
        } catch (Exception e) {
            System.err.println("Error reading " + oggFile.getName() + ": " + e.getMessage());
        }
    }
	}

	public static void debugAudioFormat(String fileName, InputStream inputStream) {
     try {
         AudioInputStream audioStream = AudioSystem.getAudioInputStream(inputStream);
         AudioFormat format = audioStream.getFormat();

         String encoding = format.getEncoding().toString();
         float sampleRate = format.getSampleRate();
         int channels = format.getChannels();
         int sampleSizeBits = format.getSampleSizeInBits();
         float frameRate = format.getFrameRate();
         int frameSize = format.getFrameSize();
         boolean isBigEndian = format.isBigEndian();

         // Check if this is a valid OGG Vorbis format
         boolean isValid = encoding.contains("VORBIS") || encoding.contains("OGG");

         // Print formatted table row
         System.out.printf("%-20s %-10s %-10.1f %-10d %-10d %-10.1f %-10d %-10s %-10s\n",
                 fileName, encoding, sampleRate, channels, sampleSizeBits, frameRate, frameSize, isBigEndian, isValid ? "✔ YES" : "❌ NO");

     } catch (Exception e) {
         System.out.printf("%-20s %-10s %-10s %-10s %-10s %-10s %-10s %-10s %-10s\n",
                 fileName, "ERROR", "-", "-", "-", "-", "-", "-", "❌ NO");
     }
 }
}
