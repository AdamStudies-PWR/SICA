import os

import cv2

import numpy as np
import pandas as pd
import tensorflow as tf
import matplotlib.pyplot as plt


# path = "../../data_set/SICA/american/"
path = "../../data_set/SICA/old_data/"

# model_name = "model_american.tflite"
model_name = "model_polish.tflite"


files = os.listdir(path)
files.sort()
print(files)

images = []
labels = []

for file in files:
    num_label = 0
    sub_dir = os.listdir(os.path.join(path, file))
    print(file + ": " + str(len(sub_dir)))
    for sub_file in sub_dir:
        img_path = os.path.join(path, file, sub_file)
        image = cv2.imread(img_path)
        image = cv2.resize(image, (96, 96))
        image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        images.append(image)
        labels.append(num_label)
    
    num_label = num_label + 1

images = np.array(images)
labels = np.array(labels)


from sklearn.model_selection import train_test_split


x_train, x_test, y_train, y_test = train_test_split(images, labels, test_size=0.1)
del images, labels


import gc
gc.collect()


from tensorflow.keras import layers, callbacks, utils, applications, optimizers
from tensorflow.keras.models import Sequential, Model, load_model


model = Sequential()

# This can be tweaked
pretrained = tf.keras.applications.EfficientNetB0(input_shape=(96, 96, 3), include_top=False)
model.add(pretrained)

# Layers (also can be tweaked)
model.add(layers.GlobalAveragePooling2D())
model.add(layers.Dropout(0.3))
model.add(layers.Dense(1))
model.build(input_shape=(None, 96, 96, 3))

model.summary()

# Model compilation, once again, to be tweaked
model.compile(optimizer="adam", loss="mae", metrics=["mae"])

checkpoint_path = "checkpoint/model"
checkpoint = callbacks.ModelCheckpoint(filepath=checkpoint_path, monitor="val_mae", mode="auto", save_best_only="True",
    save_weights_only=True)


reducer = callbacks.ReduceLROnPlateau(factor=0.9, monitor="val_mae", mode="auto", cooldown=0, patience=5, verbose=1,
    min_lr=1e-6)

# Setup training
Epochs = 100
Batch_Size = 32

history = model.fit(x_train, y_train, validation_data=(x_test, y_test), batch_size=Batch_Size, epochs=Epochs,
    callbacks=[checkpoint, reducer])

model.load_weights(checkpoint_path)

converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

with open(model_name, "wb") as file:
    file.write(tflite_model)

validator = model.predict(x_test, batch_size=32)
print(validator[:10])
print(y_test[:10])
