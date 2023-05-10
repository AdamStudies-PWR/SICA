FROM fedora:33

RUN dnf install --refresh -y \
    python3 \
    python3-pip \
    python3-devel \
    python3-wheel \
    gcc

RUN mkdir /app
WORKDIR /app

COPY requirements.txt /app
RUN pip install -r requirements.txt

COPY . /app

ENV pythonunbuffered 1
CMD uvicorn main:app --reload
