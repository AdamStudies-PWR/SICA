import os

import cv2

import numpy as np
import pandas as pd
import tensorflow as tf
import matplotlib.pyplot as plt

from tqdm import tqdm


# path = "../../data_set/SICA/american/"
path = "../../data_set/SICA/old_data/"

files = os.listdir(path)
files.sort()
print(files)

images = []
labels = []

for file in files:
    sub_dir = os.listdir(os.path.join(path, file))
    print(file + ": " + str(len(sub_dir)))
    for sub_file in sub_dir:
        img_path = os.path.join(path, file, sub_file)
        image = cv2.imread(img_path)
        image = cv2.resize(image, (96, 96))
        image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        images.append(image)
        labels.append(file)


images = np.array(images)
labels = np.array(labels)


from sklearn.model_selection import train_test_split


x_train, x_test, y_train, y_test = train_test_split(images, labels, test_size=0.1)


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

