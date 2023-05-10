# Backendzik

## Installation 

```bash
virtualenv venv
. venv/bin/activate
pip install -r requirements.txt
```

## Running 

```bash
uvicorn main:app --reload
```

... Or use the included Docker container, expose port 8000

## Usage

Check http://localhost:8000/docs for documentation.

*Note:* Current functionality (file type recognition) can be checked with the form in `index.html`
