from typing import Optional

import numpy as np
import tensorflow as tf
import magic
import io
import sys

import uvicorn
from PIL import Image
from pydantic import BaseModel
from typing import List
from fastapi import FastAPI, File, UploadFile, HTTPException
from tensorflow import keras
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

filepath = '/home/pjoter/ziwg-projekt/ml/New_model_at_90.h5'
model = keras.models.load_model(filepath, compile=True)
image_size = (180, 180)

origins = [
    "http://localhost:3000",
    "localhost:3000"
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"]
)

SIGNS = ["1", "2", "3", "4", "5", "A", "B", "C", "D", "E", "F", "G", "H", "I", "K", "L", "M", "N", "O", "P", "R", "S",
         "T", "U", "W", "Y", "Z"]


def predict(image: Image.Image):
    image = np.asarray(image.resize((180, 180)))[..., :3]
    image = np.expand_dims(image, 0)
    predictions = model.predict(image)

    return predictions


@app.post("/recognize/")
async def upload_photo_to_recognize(picture: UploadFile = File(...)):
    if picture.content_type.startswith('image/') is False:
        raise HTTPException(status_code=400, detail=f'File \'{picture.filename}\' is not an image.')

    try:
        # Read image contents
        contents = await picture.read()
        image = Image.open(io.BytesIO(contents))
        predictions = predict(image)
        # Generate prediction
        L = np.argsort(-predictions, axis=1)
        best = int(L[:, 0])
        second = int(L[:, 1])
        return {"firstSign": SIGNS[best], "firstProbability": float(predictions[0][best]),
                "secondSign": SIGNS[second], "secondProbability": float(predictions[0][second])}

    except:
        e = sys.exc_info()[1]
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
