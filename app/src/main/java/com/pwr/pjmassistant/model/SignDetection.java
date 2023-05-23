package com.pwr.pjmassistant.model;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.EditText;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SignDetection
{
    private final String TAG = "model.SignDetection";

    private final String modelPath;
    private final String labelPath;

    private final int INPUT_SIZE;

    private Interpreter interpreter;

    private List<String> labelList;

    private final int threshold;
    private int occurrences = 0;
    private String previousLetter = "";
    private boolean placed = false;

    public SignDetection(String modelName, String labelName, int inputSize, int threshold)
    {
        this.modelPath = "sign_models/" + modelName;
        this.labelPath = "sign_models/" + labelName;
        this.INPUT_SIZE = inputSize;
        this.threshold = threshold;
    }

    public boolean tryLoadModel(AssetManager manager)
    {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);

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
        AssetFileDescriptor fileDescriptor = manager.openFd(modelPath);
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
                new InputStreamReader(manager.open(labelPath)));

        String line;
        while((line = reader.readLine()) != null)
        {
            labels.add(line);
        }
        reader.close();

        return labels;
    }

    public Mat getSign(HandData data, EditText outputText)
    {
        Log.d(TAG, "getSign");

        Mat inputImage = data.getImageData();
        if (!data.isHandDetected()) { return rotateImage(inputImage); }

        Float[] boundingBox = data.getBoundingBox();
        float top = boundingBox[0];
        float bottom = boundingBox[1];
        float left = boundingBox[2];
        float right = boundingBox[1];

        if (top < 0) top = 0;
        if (left < 0) left = 0;
        if (right > data.getWidth()) right = data.getWidth();
        if (bottom > data.getHeight()) bottom = data.getHeight();

        if (left >= right || top >= bottom) return rotateImage(inputImage);

        float width = right - left;
        float height = bottom - top;

        if (width < 1 || height < 1) return rotateImage(inputImage);

        Rect hand = new Rect((int) left, (int) top, (int) width, (int) height);
        Mat cropped = new Mat(inputImage, hand).clone();

        Bitmap bitmap = Bitmap.createBitmap(cropped.cols(), cropped.rows(),
                Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(cropped, bitmap);

        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        ByteBuffer bufferedHand = convertBitmapToBytes(bitmap);

        float[][] output = new float[1][1];

        interpreter.run(bufferedHand, output);

        String letter = labelList.get((int) output[0][0]);

        if (!Objects.equals(letter, previousLetter))
        {
            resetCounter(letter);
        }
        else occurrences++;

        if (occurrences >= threshold && !placed)
        {
            placed = true;
            String temp = outputText.getText() + letter;
            outputText.setText(temp);
        }

        Imgproc.rectangle(inputImage, new Point(left, top), new Point(right, bottom),
                new Scalar(0, 0, 255, 255), 3);
        Imgproc.putText(inputImage, letter, new Point(left, top),
                3, 2, new Scalar(255, 0, 0, 255), 2);

        return rotateImage(inputImage);
    }

    private void resetCounter(String letter)
    {
        previousLetter = letter;
        occurrences = 0;
        placed = false;
    }

    private Mat rotateImage(Mat input)
    {
        Mat output = new Mat();
        Mat temp = input.t();
        Core.flip(temp, output, Core.ROTATE_90_CLOCKWISE);
        temp.release();
        return output;
    }

    private ByteBuffer convertBitmapToBytes(Bitmap bitmap)
    {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3);
        buffer.order(ByteOrder.nativeOrder());

        int[] values = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(values, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(),
                bitmap.getHeight());

        int pixels = 0;
        for (int i = 0; i < INPUT_SIZE; ++i)
        {
            for (int j = 0; j < INPUT_SIZE; ++j)
            {
                final int value = values[pixels++];
                buffer.putFloat((value >> 16) & 0xFF);
                buffer.putFloat((value >> 8) & 0xFF);
                buffer.putFloat((value) & 0xFF);
            }
        }

        return buffer;
    }
}
