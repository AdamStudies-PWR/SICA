package com.pwr.pjmassistant.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.task.vision.classifier.Classifications;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier;

import java.io.IOException;
import java.util.List;

public class Model
{
    private final String TAG = "MODEL";
    private String modelName;

    private ImageClassifier model;

    public Model(String modelName)
    {
        this.modelName = modelName;
    }

    public boolean tryLoadModel(Context context)
    {
        try
        {
           model = ImageClassifier.createFromFile(context, modelName);
           return true;
        } catch (IOException error)
        {
            Log.e(TAG, "Failed to load model file: " + error);
            return false;
        }
    }

    public String recognizeSymbol(Bitmap image)
    {
        if (model == null)
        {
            return "-";
        }

        ImageProcessor processor = new ImageProcessor.Builder().build();
        TensorImage tensor = processor.process(TensorImage.fromBitmap(image));

        // model.classify(tensor);
        List<Classifications> result = model.classify(tensor);

        return classificationToResult(result);
    }

    private String classificationToResult(List<Classifications> result)
    {
        // TODO: Do something with input and get decoded letter

        return "ඞ";
    }

}
