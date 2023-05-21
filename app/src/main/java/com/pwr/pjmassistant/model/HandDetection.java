package com.pwr.pjmassistant.model;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HandDetection
{
    private final String TAG = "model.HandDetection";
    private final String modelName;

    private Interpreter interpreter;
    private List<String> labelList;

    private final int INPUT_SIZE;
    private int PIXEL_SIZE = 3;
    private int IMAGE_MEAN = 0;
    private final float IMAGE_STD = 255.0f;

    private int height = 0;
    private int width = 0;

    private GpuDelegate gpuDelegate;

    public HandDetection(String modelName, int inputSize)
    {
        this.modelName = modelName;
        this.INPUT_SIZE = inputSize;
    }

    public boolean tryLoadModel(AssetManager manager)
    {
        Interpreter.Options options = new Interpreter.Options();
        gpuDelegate = new GpuDelegate();
        // options.addDelegate(gpuDelegate);
        options.setNumThreads(6);

        try
        {
            interpreter = new Interpreter(loadModel(manager), options);
            labelList = loadLabels(manager);
            return true;
        }
        catch (IOException error)
        {
            Log.e(TAG, "Failed to load model: " + error.getMessage());
            return false;
        }
    }

    private ByteBuffer loadModel(AssetManager manager) throws IOException
    {
        AssetFileDescriptor fileDescriptor = manager.openFd(modelName);
        FileInputStream input = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel channel = input.getChannel();
        long offset = fileDescriptor.getStartOffset();
        long length = fileDescriptor.getDeclaredLength();

        return channel.map(FileChannel.MapMode.READ_ONLY, offset, length);
    }

    private List<String> loadLabels(AssetManager manager) throws IOException
    {
        List<String> labels = new ArrayList<>();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(manager.open("labelmap.txt")));

        String line;
        while((line = reader.readLine()) != null)
        {
            labels.add(line);
        }
        reader.close();

        return labels;
    }

    public Mat getHand(Mat inputImage)
    {
        Log.d(TAG, "getHand");

        Mat rotatedImage = new Mat();
        Core.flip(inputImage.t(), rotatedImage, 1);

        Bitmap bitmap = Bitmap.createBitmap(rotatedImage.cols(), rotatedImage.rows(),
                Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rotatedImage, bitmap);
        height = bitmap.getHeight();
        width = bitmap.getWidth();
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

        ByteBuffer bufferedImage = convertBitmapToBytes(bitmap);

        Object[] input = new Object[1];
        input[0] = bufferedImage;

        float[][][] boundingBoxes = new float[1][10][4];
        float[][] scores = new float[1][10];
        float[][] classes = new float[1][10];

        Map<Integer, Object> output = new TreeMap<>();
        output.put(0, boundingBoxes);
        output.put(1, classes);
        output.put(2, scores);

        interpreter.runForMultipleInputsOutputs(input, output);

        Object boxes = output.get(0);
        Object predictions = output.get(1);
        Object prediction_scores = output.get(2);

        for (int i=0; i<10; i++)
        {
            float prediction = (float) Array.get(Array.get(predictions, 0), i);
            float score = (float) Array.get(Array.get(prediction_scores, 0), i);

            if (score > 0.5)
            {
                Object box = Array.get(Array.get(boxes, 0), i);
                float top = (float) Array.get(box, 0) * height;
                float bottom = (float) Array.get(box, 2) * height;
                float left = (float) Array.get(box, 1) * width;
                float right = (float) Array.get(box, 3) * width;

                Imgproc.rectangle(rotatedImage, new Point(left, top), new Point(right, bottom),
                        new Scalar(0, 0, 255, 255), 3);
                Imgproc.putText(rotatedImage, labelList.get((int) prediction), new Point(left, top),
                        3, 2, new Scalar(255, 0, 0, 255), 2);
            }
        }

        Core.flip(rotatedImage.t(), inputImage, Core.ROTATE_90_CLOCKWISE);
        return inputImage;
    }

    private ByteBuffer convertBitmapToBytes(Bitmap bitmap)
    {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3);
        buffer.order(ByteOrder.nativeOrder());

        int[] values = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(values, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(),
                bitmap.getHeight());

        int pixels = 0;
        for (int i=0; i<INPUT_SIZE; ++i)
        {
            for (int j=0; j<INPUT_SIZE; ++j)
            {
                final int value = values[pixels++];
                buffer.putFloat(((value >> 16) & 0xFF) / IMAGE_STD);
                buffer.putFloat(((value >> 8) & 0xFF) / IMAGE_STD);
                buffer.putFloat(((value) & 0xFF) / IMAGE_STD);
            }
        }

        return buffer;
    }
}
