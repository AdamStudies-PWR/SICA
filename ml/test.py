import numpy as np
import tensorflow as tf


from tensorflow import keras
from tensorflow.keras import layers
from tensorflow.keras.models import Sequential

image_size = (180, 180)

if __name__ == "__main__":
    model = keras.models.load_model("save_at_50.h5")
    img = keras.preprocessing.image.load_img(
        "/home/pjoter/Downloads/image.png", target_size=image_size
    )
    img_array = keras.preprocessing.image.img_to_array(img)
    img_array = tf.expand_dims(img_array, 0)  # Create batch axis

    predictions = model.predict(img_array)
    score = predictions[0]
    print(predictions)
    print(np.argmax(predictions)+1)
