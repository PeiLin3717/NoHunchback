package org.example;

import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // Create a JFrame to display the video feed
        JFrame window = new JFrame("Camera View");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close operation
        JLabel label = new JLabel(); // JLabel to hold the image
        window.getContentPane().add(label); // Add the label to the JFrame
        //window.pack();
        window.setSize(640, 480);
        window.setVisible(true);

        // Set up a frame grabber to capture video from the default camera
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.start(); // Start the frame grabber

        // Converter to convert frames grabbed by JavaCV into OpenCV Mat objects
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

        // Load the face detection model
        CascadeClassifier faceDetector = new CascadeClassifier("src/haarcascade_frontalface_default.xml");

        //import audio as notifier
        File notifier = new File("media/too close.wav");
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(notifier);
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);

        // Infinite loop to continuously grab and process frames from the camera
        while (true) {
            Frame grabbedFrame = grabber.grab(); // Grab a frame from the camera
            Mat mat = converter.convert(grabbedFrame); // Convert the frame to Mat format

            // Check if the frame is not null and not empty
            if (mat != null && !mat.empty()) {
                RectVector faces = new RectVector(); // Container for detected faces

                // Detect faces in the frame and store their positions in 'faces'
                faceDetector.detectMultiScale(mat, faces);

                // Iterate over detected faces and draw rectangles around them
                for (int i = 0; i < faces.size(); i++) {
                    Rect face = faces.get(i); // Get the face rectangle
                    int faceDistance = face.area();
                    if (faceDistance > 250000 && !clip.isRunning()) {
                        clip.stop(); // Stop the clip if it's currently running
                        clip.setMicrosecondPosition(0); // Rewind to the beginning of the clip
                        clip.start(); // Start playing the audio clip from the beginning
                    }
                    rectangle(mat, face, Scalar.RED); // Draw a red rectangle around the face
                }

                // Convert the processed Mat back to a BufferedImage for display
                BufferedImage img = new Java2DFrameConverter().convert(converter.convert(mat));

                // Set the BufferedImage as an icon of the label for display
                ImageIcon imageIcon = new ImageIcon(img);
                label.setIcon(imageIcon);

                // Adjust the window size to fit the updated image
                window.pack();
            }
        }
    }
}
