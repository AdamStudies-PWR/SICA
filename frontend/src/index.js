import React from 'react';
import ReactDOM from 'react-dom';
import {CameraFeed} from './components/camera-feed';

import './styles.css';

class App extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            firstSign: "",
            firstP: "",
            secondSign: "",
            secondP: ""
        };
    }


// Upload to local seaweedFS instance
    const
    uploadImage = async file => {
        const formData = new FormData()
        formData.append('picture', file);
        formData.append("type", 'image/jpeg')
        var link = document.createElement("a");
     //   link.download = "image.png";
      //  link.href = URL.createObjectURL(file);
       // link.click()
        console.log("Send image");
        const requestOptions = {
            method: 'POST',
            body: formData
        };
        fetch('http://localhost:8000/recognize/', requestOptions)
            .then(response => response.json())
            .then((data) => {
                console.log(data)
                this.setState({
                    firstSign: data.firstSign,
                    firstP: data.firstProbability,
                    secondSign: data.secondSign,
                    secondP: data.secondProbability
                });
            })
            .catch((err) => {
                console.error(err);
            });

    };


    render() {
        return (
            <div className="App">
                <h1>Sign language recognition</h1>
                <p>Capture Image and see result</p>
                <div id={"first"}>
                    <CameraFeed sendFile={this.uploadImage}/>
                </div>
                <div id={"second"}>
                    <p>First: {this.state.firstSign}</p>
                    <p>First probability: {this.state.firstP}</p>
                    <p>Second: {this.state.secondSign}</p>
                    <p>Second probability: {this.state.secondP}</p>
                </div>
            </div>
        );
    }
}

const rootElement = document.getElementById('root');
ReactDOM.render(<App/>, rootElement);
